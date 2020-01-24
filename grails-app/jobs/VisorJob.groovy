package visor

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat

class VisorJob {

    def lecturasService
    def dbConnectionService

    static triggers = {

        simple startDelay: 1000 * 60 * 1, repeatInterval: 1000 * 60 * 60 * 2  /* cada 10 minutos */

//        simple startDelay: 1000 * 3, repeatInterval: 1000 * 60 * 60 * 50  /* nunca */
//        simple startDelay: 1000 * 3, repeatInterval: 1000 * 10  /* cada 30 segundos */
    }


    /**
     * cada intervalo ejecutar:
     * 1. verifica si hay archivos nuevos y los copia a data y dataIUV  --> mueveArch()
     * 2. ejecuta cargar datos              --> leeCSV(tipo)
     * 3. ejecuta cargar IUV                --> cargaIUV()
     * 4. ejecuta calcular datos derivados  --> calcular()
     * 5. ejecuta calcular dirección        --> calcularDir()
     **/
    void execute() {
        println ">>> Ejecuta procesos automáticos: ${new Date()}"
        lecturasService.mueveArch()

//        cargaArchivo('prueba')

        println ">>> Inicia cargado de datos de archivos en ../data: ${new Date()}"
        cargaArchivo('prod')
        calcular()
        calcularDir()
        activar()
        calcularHoy()
        calcularDirHoy()
        println "Fin procesos automáticos: ${new Date()}"
    }


    def cargaArchivo(tipo) {
        def contador = 0
        def cn = dbConnectionService.getConnection()
        def estc
        def rgst = []
        def cont = 0
        def repetidos = 0
        def procesa = 5
        def crea_log = false
        def inserta
        def fcha
        def magn
        def sqlp
        def directorio
        def prob_magn = "No se encontró la magnitud para"
        def prob_arch = "PROBLEMAS ${new Date()}"
        def problemas = 0

        if (grails.util.Environment.getCurrent().name == 'development') {
//            directorio = '/home/guido/proyectos/visor/dataIUV/2018'
            directorio = '/home/guido/proyectos/visor/data'
        } else {
            directorio = '/home/data/data/'
        }

        if (tipo == 'prueba') { //botón: Cargar datos Minutos
            procesa = 5
            crea_log = false
        } else {
            procesa = 100000000000
            crea_log = true
        }

        def nmbr = ""
        def arch = ""
        def mg = ""
        new File(directorio).traverse(type: groovy.io.FileType.FILES, nameFilter: ~/.*\.csv/) { ar ->
            nmbr = ar.toString() - directorio
            arch = nmbr.substring(nmbr.lastIndexOf("/") + 1)

            /*** procesa las 5 primeras líneas del archivo  **/
            def line
            def cuenta = 0
            cont = 0
            repetidos = 0
            ar.withReader('UTF-8') { reader ->
                sqlp = "select count(*) cnta from survey.file where name ilike '${arch}'"
//                println "... $sqlp"
                def procesado = cn.rows(sqlp.toString())[0].cnta

                if (!procesado) {
//                    println "Cargando datos desde: $arch"
                    print "Cargando archivo: $ar "   /** menaje de cargado de archivo **/
                    while ((line = reader.readLine()) != null) {
                        if (cuenta < procesa) {
//                        println "${line}"

                            if (line.toString() =~ /\d.[a-z]./) {
                                line = line.replace(",", ".")
                            }

                            rgst = line.split(';')
                            rgst = rgst*.trim()

//                        println "***** $rgst   cuenta: $cuenta"

                            if (cuenta == 0) {
                                estc = lecturasService.datosEstaciones(rgst)
//                                if(estc && cuenta == 0) cuenta = 1
//                                println "estaciones: $estc"
                            } else if (cuenta == 1) {
//                                println ">>cuenta: $cuenta, registro: $rgst"
                                if (rgst[1].toString().contains('Ed')) {
                                    rgst = rgst*.replaceAll(('EdPAR'), ('PAR'))
                                }
                                mg = rgst[1..-1]
//                                println "buisca magn: $mg"
                                magn = lecturasService.buscaMagnIUV(mg)
//                                println "-----> mag: ${magn[0]} ---> $rgst"
                                if (magn[0] == null) {
                                    prob_magn += "\n${mg}"
                                    lecturasService.archivoProblema(arch, mg)
                                    problemas++
                                }
//                                println ">>>> ${nmbr} --> ${arch} --> ${mg} --> magn: $magn"

                            } else if (rgst[0] && rgst[0] != 'FECHA' && rgst[0].toString().toLowerCase() != 'date') {
//                            println "\n cuenta: $cuenta, fecha: ${rgst[0]}"
//                                fcha = new Date().parse('yyyy-MM-dd HH:mm:ss', rgst[0])
                                if (rgst[0].toString() =~ /\d.[a-z]./) {
                                    fcha = new SimpleDateFormat("dd-MMM-yyyy HH:mm").parse(rgst[0])
                                } else {
                                    fcha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rgst[0])
                                }

                                rgst[0] = fcha
//                            println "---> Registro: $rgst"

                                if (magn[0] && estc[0]) {
                                    inserta = lecturasService.cargarLectIUV(rgst, magn, estc)
                                    cont += inserta.insertados
                                    repetidos += inserta.repetidos
                                } else {
                                    prob_arch += "\n${ar.toString()}"
                                    problemas++
                                }
                            }

//                            if(rgst.size() > 0 && rgst[1]) cuenta++  /* se cuentan sólo si hay valores */
                            if (cuenta > 3 && rgst[1] != 0) {  /* se cuentan sólo si hay valores */
                                cuenta++
                            } else {
                                cuenta++
                            }

                        }
                    }
//                if(true) {
                    if (crea_log) {
//                    println "--- file: ${arch}"
                        lecturasService.archivoSubido(arch, cont, repetidos)
                    }
                    println "--> cont: $cont, repetidos: $repetidos"   /** menaje de cargado de archivo **/
                }
            }
//            println "---> archivo: ${ar.toString()} --> cont: $cont, repetidos: $repetidos"
        }

        /* cambia datos de estaciones IUV y IRADD_UV */
//        lecturasService.cambiaEstacion(1, 51, '99, 201')
//        lecturasService.cambiaEstacion(6, 41, '99, 201')
//        lecturasService.cambiaEstacion(7, 31, '99, 201')
/*
        if(problemas) {
            println "\n${prob_magn} \n${prob_arch}"
        } else {
            println "Se han cargado ${cont} líneas de datos y han existido : <<${repetidos}>> repetidos --> problemas: $problemas"
        }
*/
        println "Se han cargado ${cont} líneas de datos y han existido : <<${repetidos}>> repetidos --> problemas: $problemas"

//        return "Se han cargado ${cont} líneas de datos y han existido : <<${repetidos}>> repetidos --> problemas: $problemas"
    }


    def calcular() {
        println "calcular job -->"
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def sql1 = ""
        def sqlp = ""
        def magn = []
        def estc = []
        def salida = ""
//        def salidaTotal = ""
        def cnta = 0
        def desde
        def hasta
        def proceso
        def fcha
        def fchaFin
        def frmtFcha = new SimpleDateFormat("yyyy-MM-dd")

        sql = "select distinct id from survey.magnitude where active = 'S' and id != 82 order by 1"
        magn = cn.rows(sql.toString())
//        println "....1"

        proceso = ['10 minutes', '1 hours', '8 hours', '24 hours', '72 hours']
        proceso.each { prcs ->
            magn.each { mg ->
//                sql = "select distinct opoint_id id from partitions.data${mg.id} where avg1m is not null order by 1"
                sql = "select distinct id id from survey.opoint where active = 'S' order by 1"
/*
                sql = "select distinct opoint_id id from survey.data where magnitude_id = ${mg.id} and " +
                        "avg1m is not null order by 1"
*/
                println "mg--> ${mg.id}"

                estc = cn.rows(sql.toString())
//            println "....2 estc: ${estc}"

                estc.each { es ->
                    sql1 = "select min(datetime)::date fcin, max(datetime)::date fcfn from survey.data " +
                            "where magnitude_id = ${mg.id} and opoint_id = ${es.id} and avg1m is not null"
//                    print "mg--> ${mg.id}: $sql1"
                    cn.eachRow(sql1.toString()) { d ->
                        if (d.fcin && d.fcfn) {
//                            println "fcin: ${d.fcin}, fcfn: ${d.fcfn}"
//                            desde = new Date().parse("yyyy-MM-dd", "${d.fcfn}")
                            desde = new SimpleDateFormat("yyyy-MM-dd").parse("${d.fcin}")
                            hasta = new SimpleDateFormat("yyyy-MM-dd").parse("${d.fcfn}")
//                            println "desde: ${desde}"
                        } else {
                            desde = new Date()
                            hasta = desde
                        }
                    }
//                    println "desde: ${desde}, hasta: ${hasta}"

                    fcha = desde
//                    fchaFin = new Date().parse("dd-MM-yyyy", "31-12-${fcha.format('yyyy')}")
//                    fchaFin = new SimpleDateFormat("dd-MM-yyyy").parse("31-12-${fcha.format('yyyy')}")
                    fchaFin = new SimpleDateFormat("dd-MM-yyyy").parse("31-12-${fcha.getYear() + 1900}")
//                    println "procesa desde ${desde} hasta: ${fchaFin}"
                    while (fcha < hasta) {
                        use(groovy.time.TimeCategory) {
//                            println "---> ${fcha} hasta ${fchaFin}"
                            sqlp = "select count(*) cnta from survey.process " +
                                    "where '${frmtFcha.format(fcha)}' between from_date and to_date and " +
                                    "'${frmtFcha.format(fchaFin)}' between from_date and to_date and " +
                                    "magnitude_id = ${mg.id} and opoint_id = ${es.id} and name ilike '${prcs}'"
//                            println "... $sqlp"
                            def procesado = cn.rows(sqlp.toString())[0].cnta
                            if (!procesado) {
//                                print "*** ${prcs} mg--> ${mg.id}, estc: ${es.id} '${fcha.format('yyyy-MM-dd')}' a '${fchaFin.format('yyyy-MM-dd')}'"
                                sql = "select * from survey.promedios(${mg.id}, ${es.id}, '${prcs}', " +
                                        "'${frmtFcha.format(fcha)}', '${frmtFcha.format(fchaFin)}')"
//                                println "sql--> $sql"
                                cn.eachRow(sql.toString()) { dt ->
                                    salida = dt.promedios
                                }

                                println " procesa ${prcs}: magnitud: $mg con estc: $es --> $salida"
                                cnta++
                                lecturasService.procesoHecho(mg.id, es.id, prcs, salida, frmtFcha.format(fcha), frmtFcha.format(fchaFin), salida)
//                                salidaTotal += salidaTotal ? "\n${salida}" : salida
                            }

                            fcha = fchaFin + 1.day
                            if (fchaFin + 1.year > hasta) {
                                fchaFin = hasta
                            } else {
                                fchaFin = fchaFin + 1.year
                            }
                        }
                    }

                }
            }

        }
//        println "Porcesado: ${salidaTotal}"
//        return "Porcesado: ${salidaTotal}"
    }


    def calcularDir() {
        println "calcularDir job --"
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def sql1 = ""
        def sqlp = ""
        def magn = []
        def estc = []
        def salida = ""
//        def salidaTotal = ""
        def cnta = 0
        def desde
        def hasta
        def proceso
        def fcha
        def fchaFin

        proceso = ['10 minutes', '1 hours', '8 hours', '24 hours', '72 hours']
        proceso.each { prcs ->

            sql = "select distinct opoint_id id from survey.data where magnitude_id = 82 and " +
                    "avg1m is not null order by 1"
//            println "ppp: $sql"
            estc = cn.rows(sql.toString())
//            println "....2 estc: ${estc}"

            estc.each { es ->
                sql1 = "select min(datetime)::date fcin, max(datetime)::date fcfn from survey.data " +
                        "where magnitude_id = 82 and opoint_id = ${es.id} and avg1m is not null"
//                    print "mg--> ${mg.id}: $sql1"
                cn.eachRow(sql1.toString()) { d ->
//                    println "fcin: ${d.fcin}, fcfn: ${d.fcfn}"
                    desde = new Date().parse("yyyy-MM-dd", "${d.fcin}")
                    hasta = new Date().parse("yyyy-MM-dd", "${d.fcfn}")
                }
//                    println "desde: ${desde}, hasta: ${hasta}"

                fcha = desde
                fchaFin = new Date().parse("dd-MM-yyyy", "31-12-${fcha.format('yyyy')}")
//                    println "procesa desde ${desde} hasta: ${hasta}"
                while (fcha < hasta) {
                    use(groovy.time.TimeCategory) {
//                            println "---> ${fcha} hasta ${fchaFin}"
                        sqlp = "select count(*) cnta from survey.process " +
                                "where '${fcha.format('yyyy-MM-dd')}' between from_date and to_date and " +
                                "'${fchaFin.format('yyyy-MM-dd')}' between from_date and to_date and " +
                                "magnitude_id = 82 and opoint_id = ${es.id} and name ilike '${prcs}'"
//                            println "... $sqlp"
                        def procesado = cn.rows(sqlp.toString())[0].cnta
                        if (!procesado) {
                            print "*** Dir: ${prcs} estc: ${es.id} '${fcha.format('yyyy-MM-dd')}' a '${fchaFin.format('yyyy-MM-dd')}'"
                            sql = "select * from survey.promedios_dir(${es.id}, '${prcs}', " +
                                    "'${fcha.format('yyyy-MM-dd')}', '${fchaFin.format('yyyy-MM-dd')}')"
//                            println "sql--> $sql"
                            cn.eachRow(sql.toString()) { dt ->
                                salida = dt.promedios_dir
                            }

                            println "procesa dirección viento ${prcs} con estc: $es --> $salida"
                            cnta++
                            lecturasService.procesoHecho(82, es.id, prcs, salida, fcha.format('yyyy-MM-dd'), fchaFin.format('yyyy-MM-dd'), salida)
//                            salidaTotal += salidaTotal ? "\n${salida}" : salida
                        }

                        fcha = fchaFin + 1.day
                        if (fchaFin + 1.year > hasta) {
                            fchaFin = hasta
                        } else {
                            fchaFin = fchaFin + 1.year
                        }
                    }
                }

            }

        }

//        println "Porcesado: ${salidaTotal}"
//        return "Porcesado: ${salidaTotal}"
    }

    def activar() {
        println "activar -->"
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def magn = ""
        def estc = ""
        def nmro = 0
        def actv = 0

        sql = "select activate from prmt"
        actv = cn.rows(sql.toString())[0].activate
        cn.execute("update prmt set activate = activate + 1")
        if (actv % 12 == 0) {
            cn.execute("update prmt set activate = 1")
            /* procesa activar estaciones y magnitudes */
            sql = "select distinct magnitude_id id from survey.data"
            cn.eachRow(sql.toString()) { m ->
                magn += magn ? ", ${m.id}" : m.id
            }
            println "magnitudes $magn"

            sql = "select distinct opoint_id id from survey.data"
            cn.eachRow(sql.toString()) { e ->
                estc += estc ? ", ${e.id}" : e.id
            }
            println "estaciones $estc"

            sql = "update survey.magnitude set active = 'S' where id in ($magn)"
//        println "sql --> $sql"
            cn.execute(sql.toString())
            nmro += cn.updateCount

            sql = "update survey.magnitude set active = 'N' where id not in ($magn)"
//        println "sql --> $sql"
            cn.execute(sql.toString())
            nmro += cn.updateCount

            sql = "update survey.opoint set active = 'S' where id in ($estc)"
//        println "sql --> $sql"
            cn.execute(sql.toString())
            nmro += cn.updateCount

            sql = "update survey.opoint set active = 'N' where id not in ($estc)"
//        println "sql --> $sql"
            cn.execute(sql.toString())
            nmro += cn.updateCount

            println "Porcesado: ... $nmro registros"
        }
    }


    def calcularHoy() {
        println "calculaHoy job -->"
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def magn = []
        def estc = []
        def salida = ""
        def proceso = ['10 minutes', '1 hours', '8 hours', '24 hours', '72 hours']
        def fchaFin = new Date()
        def fcha = fchaFin - 5

        sql = "select distinct id from survey.magnitude where active = 'S' and id != 82 order by 1"
        magn = cn.rows(sql.toString())
        proceso.each { prcs ->
            magn.each { mg ->
                print " procesa ${prcs}: magnitud: ${mg.id} "

                sql = "select distinct id id from survey.opoint where active = 'S'"
                estc = cn.rows(sql.toString())
                estc.each { es ->
                    sql = "select * from survey.promedios(${mg.id}, ${es.id}, '${prcs}', " +
                            "'${fcha.format('yyyy-MM-dd')}', '${fchaFin.format('yyyy-MM-dd')}')"
//                    println "sql--> $sql"
                    cn.eachRow(sql.toString()) { dt ->
                        salida = dt.promedios
                    }
                    print "..${es.id}"
                }
                println ""
            }
        }
    }


    def calcularDirHoy() {
        println "calcula dir hoy job -->"
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def magn = []
        def estc = []
        def salida = ""
        def proceso = ['10 minutes', '1 hours', '8 hours', '24 hours', '72 hours']
        def fchaFin = new Date()
        def fcha = fchaFin - 5

        sql = "select distinct opoint_id id from survey.data where magnitude_id = 82 and " +
                "avg1m is not null order by 1"

        estc = cn.rows(sql.toString())
//            println "....2 estc: ${estc}"
        proceso.each { prcs ->
            print " procesa dirrección ${prcs}: dirección del viento "
            estc.each { es ->
                sql = "select * from survey.promedios_dir(${es.id}, '${prcs}', " +
                        "'${fcha.format('yyyy-MM-dd')}', '${fchaFin.format('yyyy-MM-dd')}')"
                cn.eachRow(sql.toString()) { dt ->
                    salida = dt.promedios_dir
                }
                print "..${es.id}"
            }
            println ""
        }
    }
}