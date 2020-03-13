package visor

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat

class VisorJob {

    def lecturasService
    def dbConnectionService

    static triggers = {

        simple startDelay: 1000 * 60 * 1, repeatInterval: 1000 * 60 * 60 * 2  /* cada 2 horas */
//
//        simple startDelay: 1000 * 60*60, repeatInterval: 1000 * 60 * 60 * 50  /* nunca */
//        simple startDelay: 1000 * 3, repeatInterval: 1000 * 60 * 30 /* a los 3 segundos -- repite cada 30 min */
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

// ********* pruebas **********
//        cargaArchivo('prueba')
//        cargaArchHora('prueba')
//        verificar()
//        data_nasa('prueba')
// ** fin ** pruebas **********

        println ">>> Inicia cargado de datos de archivos en ../data: ${new Date()}"
        data_nasa('prod')  // carga datos de forecasting NASA

        cargaArchivo('prod')
        cargaArchHora('prod')
        calcular()
        calcularDir()
        activar()
        calcularHoy()
        calcularDirHoy()


/*
        def sout = new StringBuilder(), serr = new StringBuilder()
        def cmnd = '/home/guido/proyectos/visor/hace_bk.sh'
//        def cmnd = '/home/hace_bk.sh'
        def cont = 35
        while(cont > 7) {
            cont = cont<0? 0 : cont-7
            println "$cmnd ${cont + 7} ${cont}"
            def proc = "$cmnd ${cont + 7} ${cont}".execute()
            proc.consumeProcessOutput(sout, serr)
            proc.waitForOrKill(20000)
            sleep(25000)
        }
//        println "out> $sout err> $serr"
        println "proceso de backup de data ejecutado"
*/
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
//                                println "estaciones: $estc" //* revisar *//
                            } else if (cuenta == 1) {
//                                println ">>cuenta: $cuenta, registro: $rgst"
                                if (rgst[1].toString().contains('Ed')) {
                                    rgst = rgst*.replaceAll(('EdPAR'), ('PAR'))
                                }
//                                println "busca magn: $rgst"
                                mg = rgst[1..-1]
                                magn = lecturasService.buscaMagnIUV(mg)
//                                println "-----> mag: ${magn[0]} ---> $rgst"
                                if (magn[0] == null) {
                                    prob_magn += "\n${mg}"
                                    lecturasService.archivoProblema(arch, mg)
                                    problemas++
                                }
//                                println ">>>> ${nmbr} --> ${arch} --> ${mg} --> magn: $magn"

//                            } else if (rgst[0] && rgst[0] != 'FECHA' && rgst[0].toString().toLowerCase() != 'date' && rgst[0]?.size() > 8) {
                            } else if (rgst[0] && rgst[0] != 'FECHA' && rgst[0].toString().toLowerCase() != 'date') {
//                            println "\n cuenta: $cuenta, fecha: ${rgst[0]}"
//                                fcha = new Date().parse('yyyy-MM-dd HH:mm:ss', rgst[0])
                                fcha = null
                                try {
                                    if (rgst[0].toString() =~ /\d.[a-z]./) {
                                        fcha = new SimpleDateFormat("dd-MMM-yyyy HH:mm").parse(rgst[0])
                                    } else {
                                        if (rgst[0].toString() =~ '/') {
                                            fcha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(rgst[0])
                                        } else {
                                            fcha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rgst[0])
                                        }
                                    }

                                    rgst[0] = fcha
                                } catch (Exception er) {
                                    println "error en fecha del regitro ${rgst[0]}, arch: $ar, err: $er"
                                }
//                            println "---> Registro: $rgst" //* revisar *//

                                if (magn[0] && estc[0] && fcha) {
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

    def cargaArchHora(tipo) {
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
            directorio = '/home/guido/proyectos/visor/dataHora'
        } else {
            directorio = '/home/data/dataHora'
        }

        if (tipo == 'prueba') { //botón: Cargar datos Minutos
            procesa = 6
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
                sqlp = "select count(*) cnta from survey.file where name ilike 'hr_${arch}'"
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
//                                println "estaciones: $estc" //* revisar *//
                            } else if (cuenta == 1) {
//                                println ">>cuenta: $cuenta, registro: $rgst"
                                if (rgst[1].toString().contains('Ed')) {
                                    rgst = rgst*.replaceAll(('EdPAR'), ('PAR'))
                                }
//                                println "busca magn: $rgst"
                                mg = rgst[1..-1]
//                                println "--mg: $mg"
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
                                    if (rgst[0].toString() =~ '/') {
                                        fcha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(rgst[0])
                                    } else {
                                        fcha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rgst[0])
                                    }
                                }

                                rgst[0] = fcha
//                            println "---> Registro: $rgst" //* revisar *//

                                if (magn[0] && estc[0]) {
//                                    inserta = lecturasService.cargarLectIUV(rgst, magn, estc)
                                    inserta = lecturasService.cargarLectHora(rgst, magn, estc)
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
//                        lecturasService.archivoSubido(arch, cont, repetidos)
                        lecturasService.archivoHoras(arch, cont, repetidos)
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


    def verificar() {
        println "verificar job -->"
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def magn = []

        sql = "select distinct id from survey.magnitude where active = 'S' order by 1"
        magn = cn.rows(sql.toString())
//        println "....1"

        magn.each { mg ->
            sql = "select * from survey.verifica_data(${mg.id});"
            println "mg--> ${mg.id}"
            cn.eachRow(sql.toString()) { d ->
                println "${d.verifica_data}"

            }
        }
    }


    def data_nasa(tipo) {
        def contador = 0
        def cn = dbConnectionService.getConnection()
        def rgst = []
        def data = []
        def cont = 0
        def repetidos = 0
        def procesa = 5
        def crea_log = false
        def inserta
        def fcha
        def magn
        def sqlp
        def directorio
        def problemas = 0
        def dire = 0.0, vlcd = 0.0, cx = 0.0, cy = 0.0

        if (grails.util.Environment.getCurrent().name == 'development') {
//            directorio = '/home/guido/proyectos/visor/dataIUV/2018'
            directorio = '/home/guido/proyectos/visor/nasa'
        } else {
            directorio = '/home/data/nasa'
        }

        if (tipo == 'prueba') { //botón: Cargar datos Minutos
            procesa = 6
            crea_log = false
        } else {
            procesa = 100000000000
            crea_log = true
        }

        def mg = ""
        def arch = new File("${directorio}/data_nasa_hoy.csv")

        def sql = "select count(*) cnta from survey.file_forecast where date_file= " +
                "'${new Date().format('yyyy-MM-dd')}'"
//        println "sql: $sql"
        def procesado = cn.rows(sql.toString())[0].cnta

        if(procesado) {
            println "ya se ha procesado el archivo de la nasa"
            return
        }

        //nuevo
        def line
        def cuenta = 0
        cont = 0
        repetidos = 0
        arch.withReader('UTF-8') { reader ->
            print "Cargando archivo NASA: $arch "
            while ((line = reader.readLine()) != null) {
                if (cuenta < procesa) {
//                    println "${line}"

                    rgst = line.split(',')
                    rgst = rgst*.trim()

//                    println "***** $rgst   cuenta: $cuenta"

                    if (cuenta == 0) {
                        mg = rgst[5..-1]
//                        println "--mg: $mg"
                        magn = lecturasService.magnitudNasa(mg)
//                        println "-----> mag: ${magn}"

                    } else if (rgst[0]) {
//                            println "\n cuenta: $cuenta, fecha: ${rgst[0]}"
                        fcha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").parse(rgst[0])
//                        println "fecha: ${fcha.format('yyyy-MM-dd hh:mm')}"

                        rgst[0] = fcha

                        cx = rgst[8].toDouble()
                        cy = rgst[9].toDouble()
//                        println "cx: $cx, cy: $cy, --> ${Math.pow(cx, 2)}"
                        vlcd = Math.sqrt(Math.pow(cx, 2) + Math.pow(cy, 2))
                        dire = (Math.toDegrees(Math.atan2(cy, cx)) + 360) % 360
//                        println "${rgst[8].toDouble()}, ${rgst[9].toDouble()} --> $vlcd  -- $dire"

                        data = [rgst[0], rgst[2], rgst[5], rgst[6], rgst[7], vlcd, dire, rgst[11], rgst[13], rgst[15]]

//                        println "---> Registro: $data" //* revisar *//

                        if (data[0]) {
                            inserta = lecturasService.cargaForcast(data, magn)
                            cont += inserta.insertados
                            repetidos += inserta.repetidos
                        } else {
                            prob_arch += "\n${ar.toString()}"
                            problemas++
                        }

                    }
                    cuenta++

//                    println " ........... fin registro ......"
                }
            }
            if (crea_log) {
                println "crea log de file: ${arch}"
                lecturasService.archivoNasa(arch, cont, repetidos)
            }
            println "--> cont: $cont, repetidos: $repetidos"   /** menaje de cargado de archivo **/
        }
        println "Se han cargado ${cont} líneas de datos y han existido : <<${repetidos}>> repetidos --> problemas: $problemas"
    }


}