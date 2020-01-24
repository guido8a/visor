package apli

import grails.gorm.transactions.Transactional

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat

@Transactional
class LecturasService {

    def dbConnectionService

    def serviceMethod() {

    }


    /**
     * Mueve archivos desde un repositorio común a las carpetas de data para cargarlos a la BD
     * los archivos nuevos se deben poner en /home/data/remarq y de ahí se distribuyen a
     * --> /home/data/data o /home/data/dataIUV si son nuevos
     * */
    def mueveArch() {
        println ">>mueveArch.."
        def cn = dbConnectionService.getConnection()
        def cn_data, cn_iuv
        def dir_iuv, dir_data, dir_arch, dir_dataN, dir_iuvN

        if(grails.util.Environment.getCurrent().name == 'development') {
            dir_data = '/home/guido/proyectos/visor/data/'
            dir_iuv = '/home/guido/proyectos/visor/dataIUV/'
            dir_arch = '/home/guido/proyectos/visor/remaq/'
        } else {
            dir_data = '/home/data/data/'
//            dir_iuv = '/home/data/dataIUV/'
            dir_arch = '/home/data/remaq/'
        }

        cn_data = 0
        cn_iuv = 0

        def nmbr = "", arch = "", sqlp = ""

        new File(dir_arch).traverse(type: groovy.io.FileType.FILES, nameFilter: ~/.*\.csv/) { ar ->
            nmbr = ar.toString() - dir_arch
            arch = nmbr.substring(nmbr.lastIndexOf("/") + 1)

            sqlp = "select count(*) cnta from survey.file where name ilike '${arch}'"
//            println "... $sqlp"
            def procesado = cn.rows(sqlp.toString())[0].cnta

            if(!procesado) {
                File original = new File(ar.toString())
                File destino

/*
                if(ar.toString().toLowerCase().contains('iuv') || ar.toString().toLowerCase().contains('sol_')) {
                    def tx = nmbr.substring(nmbr.indexOf('/') + 1, nmbr.size())
                    destino  = new File(dir_iuv + tx)
                    cn_iuv++
                } else {
*/
                    destino  = new File(dir_data + nmbr)
                    cn_data++
//                }
//                println "------------ copiando archivo: ${ar.toString()}, a: ${destino} --> ${cn_iuv + cn_data}"
                if (original.exists()) {
//                    println "---> ${destino.getParent()}"
                    File dire = new File(destino.getParent())
                    File subd = new File(dire.getParent())
                    File sub2 = new File(subd.getParent())
                    sub2.mkdir()
                    subd.mkdir()
                    dire.mkdir()
                    Files.copy(Paths.get(original.getAbsolutePath()), Paths.get(destino.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING)
                } else {
                    println "El fichero ${original} no existe"
                }
            } else {
//                println "++ ya existe ${arch}"
            }
//            println "---> archivo: ${ar.toString()}  --> procesado: ${procesado}"
        }

        println "Se han movido ${cn_data} archivos a ${dir_data}"

        return "Se han movido ${cn_data} archivos a ${dir_data} y ${cn_iuv} a: ${dir_iuv}"
    }




    def leeCSV(tipo) {
        println ">>leeCSV.."
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

        if(grails.util.Environment.getCurrent().name == 'development') {
            directorio = '/home/guido/proyectos/visor/data/'
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
            mg = arch.split('_')[0]
            magn = buscaMagn(mg)
//            print "Procesa el archivo: $ar"
//            println "${nmbr} --> ${arch} --> ${mg} --> magn: $magn"

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
                    print "Cargando datos desde: $ar "
                    while ((line = reader.readLine()) != null) {
                        if (cuenta < procesa) {
//                        println "${line}"

                            line = line.replace(",", ".")
                            rgst = line.split(';')
                            rgst = rgst*.trim()

//                        println "***** $rgst, --> ${rgst[0].toString().toLowerCase()}"

                            if (cuenta == 0) {
                                estc = datosEstaciones(rgst)
                                if(estc && cuenta == 0) cuenta = 1
//                            println "estaciones: $estc"
                            } else if (rgst[0] && rgst[0] != 'Date') {
//                            println "\n cuenta: $cuenta, fecha: ${rgst[0]}"
                                fcha = new Date().parse('dd-MMM-yyyy HH:mm', rgst[0])
                                rgst[0] = fcha
//                            println "---> Registro: $rgst"

                                inserta = cargarLecturas(magn, estc, rgst, tipo)
                                cont += inserta.insertados
                                repetidos += inserta.repetidos
                            }

                            if(rgst.size() > 2 && rgst[-2] != 0) cuenta++  /* se cuentan sólo si hay valores */

                        }
                    }
//                if(true) {
                    if (crea_log) {
                    print " --- file: ${arch} "
                        archivoSubido(arch, cont, repetidos)
                    }
                    println "--> cont: $cont, repetidos: $repetidos"

                }

            }
//            println "---> archivo: ${ar.toString()} --> cont: $cont, repetidos: $repetidos"
        }

        return "Se han cargado ${cont} líneas de datos y han existido : <<${repetidos}>> repetidos"

    }


    def cargaIUV(tipo) {
//        println ">>cargaIUV.."
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

        if(grails.util.Environment.getCurrent().name == 'development') {
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
//                    print "Cargando datos IUV desde: $ar "   /** menaje de cargado de archivo **/
                    while ((line = reader.readLine()) != null) {
                        if (cuenta < procesa) {
//                        println "${line}"

                            if(line.toString() =~ /\d.[a-z]./) {
                                line = line.replace(",", ".")
                            }

                            rgst = line.split(';')
                            rgst = rgst*.trim()

//                        println "***** $rgst   cuenta: $cuenta"

                            if (cuenta == 0) {
                                estc = datosEstaciones(rgst)
//                                if(estc && cuenta == 0) cuenta = 1
//                                println "estaciones: $estc"
                            } else if (cuenta ==1) {
//                                println ">>cuenta: $cuenta, registro: $rgst"
                                if(rgst[1].toString().contains('Ed')) {
                                    rgst = rgst*.replaceAll(('EdPAR'), ('PAR'))
                                }
                                mg = rgst[1..-1]
//                                println "buisca magn: $mg"
                                magn = buscaMagnIUV(mg)
//                                println "-----> mag: ${magn[0]} ---> $rgst"
                                if(magn[0] == null) {
                                    prob_magn += "\n${mg}"
                                    archivoProblema(arch, mg)
                                    problemas++
                                }
//                                println ">>>> ${nmbr} --> ${arch} --> ${mg} --> magn: $magn"

                            } else if (rgst[0] && rgst[0] != 'FECHA' && rgst[0].toString().toLowerCase() != 'date') {
//                            println "\n cuenta: $cuenta, fecha: ${rgst[0]}"
//                                fcha = new Date().parse('yyyy-MM-dd HH:mm:ss', rgst[0])
                                if(rgst[0].toString() =~ /\d.[a-z]./) {
                                    fcha = new SimpleDateFormat("dd-MMM-yyyy HH:mm").parse(rgst[0])
                                } else {
                                    fcha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rgst[0])
                                }

                                rgst[0] = fcha
//                            println "---> Registro: $rgst"

                                if(magn[0] && estc[0]) {
                                    inserta = cargarLectIUV(rgst, magn, estc)
                                    cont += inserta.insertados
                                    repetidos += inserta.repetidos
                                } else {
                                    prob_arch += "\n${ar.toString()}"
                                    problemas++
                                }
                            }

//                            if(rgst.size() > 0 && rgst[1]) cuenta++  /* se cuentan sólo si hay valores */
                            if(cuenta > 3 && rgst[1] != 0) {  /* se cuentan sólo si hay valores */
                                cuenta++
                            }  else {
                                cuenta++
                            }

                        }
                    }
//                if(true) {
                    if (crea_log) {
//                    println "--- file: ${arch}"
                        archivoSubido(arch, cont, repetidos)
                    }
//                    println "--> cont: $cont, repetidos: $repetidos"   /** menaje de cargado de archivo **/
                }
            }
//            println "---> archivo: ${ar.toString()} --> cont: $cont, repetidos: $repetidos"
        }
/*
        if(problemas) {
            println "\n${prob_magn} \n${prob_arch}"
        } else {
            println "Se han cargado ${cont} líneas de datos y han existido : <<${repetidos}>> repetidos --> problemas: $problemas"
        }
*/
        println "Se han cargado ${cont} líneas de datos y han existido : <<${repetidos}>> repetidos --> problemas: $problemas"

        return "Se han cargado ${cont} líneas de datos y han existido : <<${repetidos}>> repetidos --> problemas: $problemas"
    }


    def buscaMagn(ar) {
        def cn = dbConnectionService.getConnection()
        def sql = "select id from survey.magnitude where abbreviation ilike '${ar}' limit 1"
//        println "sql: $sql"
        return cn.rows(sql.toString())[0]?.id
    }

    /**
     * Busca los ID de las estaciones
     * **/
    def datosEstaciones(rgst) {
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def estc = []

//        if (rgst[0].toString().toLowerCase() == 'fecha') {
        rgst.removeAt(0)
        rgst.each() { rg ->
//            println "$rgst ilike '${rg}'"
            sql = "select id from survey.opoint where pname ilike '${rg[0..1]}%${rg[-4..-1]}'"
//            println "sql: $sql"
            def resp = cn.rows(sql.toString())
//                println "---> $resp"
            def id = cn.rows(sql.toString())[0]?.id
//                println "---> $rg, id: ${estc__id}"
//                estc[rg] = estc__id
            estc.add(id)
        }
//        }

        return estc
    }

    /**
     * carga todas las lecturas excepto las de IUV
     * **/
    def cargarLecturas(vrbl, estc, rgst, tipo) {
        def errores = ""
        def cnta = 0
        def insertados = 0
        def repetidos = 0
        def fcha
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def tbla = "survey.data"

        println "\n inicia cargado de datos para mag: $vrbl, .... $rgst"
        fcha = rgst[0]
        rgst.removeAt(0)  // elimina la fecha y quedan solo lecturas

        cnta = 0
        rgst.each() { rg ->
//            println "--> estación: ${estc[cnta]}, valor: $rg, tipo: ${rg.class}, ${rg.size()}"
            if (rg.toString().size() > 0) {
//                println "--> estación: ${estc[cnta]}, valor: $rg"
/*
                sql = "insert into ${tbla}(id, magnitude_id, opoint_id, datatype_id, datetime, avg1m) " +
                        "values(default, ${vrbl}, ${estc[cnta]}, 1, '${fcha.format('yyyy-MM-dd HH:mm')}', ${rg.toDouble()})"
*/
                sql = "insert into ${tbla}(id, magnitude_id, opoint_id, datatype_id, datetime, avg1m) " +
                        "values(default, ${vrbl}, ${estc[cnta]}, 1, '${fcha.format('yyyy-MM-dd HH:mm')}', ${rg.toDouble()}) " +
                        "on conflict (magnitude_id, opoint_id, datetime, datatype_id) " +
                        "do update set avg1m = ${rg.toDouble()}"
//                println "sql: $sql"
                try {
//                    println "inserta: $inserta"
                    cn.execute(sql.toString())
                    if (cn.updateCount > 0) {
                        insertados++
                    }
                } catch (Exception ex) {
                    repetidos++
//                    println "Error al insertar $ex"
                }

            }
            cnta++
        }

        return [errores: errores, insertados: insertados, repetidos: repetidos]
    }




    def calcular() {
        println "calcular -->"
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def sql1 = ""
        def sqlp = ""
        def magn = []
        def estc = []
        def salida = ""
        def salidaTotal = ""
        def cnta = 0
        def desde
        def hasta
        def proceso
        def fcha
        def fchaFin
        def frmtFcha = new SimpleDateFormat("yyyy-MM-dd")

        sql = "select distinct magnitude_id id from survey.data where magnitude_id != 82 order by 1"
        magn = cn.rows(sql.toString())
//        println "....1"

        proceso = ['10 minutes', '1 hours', '8 hours', '24 hours', '72 hours']
        proceso.each { prcs ->
            magn.each { mg ->
//                sql = "select distinct opoint_id id from partitions.data${mg.id} where avg1m is not null order by 1"
                sql = "select distinct opoint_id id from survey.data where avg1m is not null order by 1"
                println "mg--> ${mg.id}"

                estc = cn.rows(sql.toString())
//            println "....2 estc: ${estc}"

                estc.each { es ->
                    sql1 = "select min(datetime)::date fcin, max(datetime)::date fcfn from survey.data " +
                            "where magnitude_id = ${mg.id} and opoint_id = ${es.id} and avg1m is not null"
//                    print "mg--> ${mg.id}: $sql1"
                    cn.eachRow(sql1.toString()) { d ->
                        if(d.fcin && d.fcfn) {
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
                    fchaFin = new SimpleDateFormat("dd-MM-yyyy").parse("31-12-${fcha.getYear()+1900}")
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
                                procesoHecho(mg.id, es.id, prcs, salida, frmtFcha.format(fcha), frmtFcha.format(fchaFin), salida)
                                salidaTotal += salidaTotal ? "\n${salida}" : salida
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
        return "Porcesado: ${salidaTotal}"
    }


    def calcularDir() {
        println "calcularDir--"
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def sql1 = ""
        def sqlp = ""
        def magn = []
        def estc = []
        def salida = ""
        def salidaTotal = ""
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
            println "ppp: $sql"
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
                            procesoHecho(82, es.id, prcs, salida, fcha.format('yyyy-MM-dd'), fchaFin.format('yyyy-MM-dd'), salida)
                            salidaTotal += salidaTotal ? "\n${salida}" : salida
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

        return "Porcesado: ${salidaTotal}"
    }




    def procesoHecho(magn, estc, proc, txto, fcds, fchs, salida) {
        def cn = dbConnectionService.getConnection()
        def frmtFcha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        def sql = "insert into survey.process(id, magnitude_id, opoint_id, name, from_date, to_date, " +
                "datetime, result) values(default, '${magn}', '${estc}', '${proc}', '${fcds}', '${fchs}'," +
                "'${frmtFcha.format(new Date())}', '${salida}')"
//        println "...---> $sql"
        cn.execute(sql.toString())
    }

    def buscaMagnIUV(ar) {
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def magn = []
//        println "busca magnitud IUV: ${ar}"
        ar.each { m ->
            sql = "select id from survey.magnitude where abbreviation ilike '${m}' limit 1"
//            println "sql: $sql"
            magn.add(cn.rows(sql.toString())[0]?.id)
        }
//        println ".... $magn"
        return magn
    }


    def cargarLectIUV(rgst, magn, estc) {
        def errores = ""
        def cnta = 0
        def insertados = 0
        def repetidos = 0
        def fcha
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def mg_es = ""
        def xx_es = [1:51, 6:41, 7:31]

//        println "\n **inicia cargado de datos para mag: $magn, estc: ${estc}.... $rgst"
        fcha = rgst[0]
        rgst.removeAt(0)  // elimina la fecha y quedan solo lecturas

        cnta = 0
        rgst.each() { rg ->
//            println "--> estación: ${estc[cnta]}, valor: $rg, tipo: ${rg.class}, ${rg.size()}"
            if (rg.toString().size() > 0) {
//                println "--> estación: ${estc[cnta].class}, mag: ${magn[cnta].class}, valor: $rg"
                if((estc[cnta].toInteger() in xx_es.keySet()) && (magn[cnta].toInteger() in [99, 201])) {
                    mg_es = xx_es[estc[cnta].toInteger()]
//                    println "cambia estación mag: ${magn[cnta]}, estc: ${estc[cnta]} --> $mg_es"
                    print "."
                } else {
                    mg_es = estc[cnta]
                }
                sql = "insert into survey.data (id, magnitude_id, opoint_id, datatype_id, datetime, avg1m) " +
                        "values(default, ${magn[cnta]}, ${mg_es}, 1, '${fcha.format('yyyy-MM-dd HH:mm')}', ${rg.toDouble()}) " +
                        "on conflict (magnitude_id, opoint_id, datetime, datatype_id) " +
                        "do update set avg1m = ${rg.toDouble()}"
//                println "sql: $sql"

                try {
                    cn.execute(sql.toString())
//                    println "inserta: $sql"
//                    println ">> ${cn.updateCount}"
                    if (cn.updateCount > 0) {
                        insertados++
                    }
                } catch (Exception ex) {
                    repetidos++
                    println "Error al insertar $ex"
                }

            }
            cnta++
        }

        return [errores: errores, insertados: insertados, repetidos: repetidos]
    }

    def archivoSubido(arch, cont, rept) {
        def cn = dbConnectionService.getConnection()
        def sql = "insert into survey.file(id, name, loaded, lines, errors) values(default, '${arch}', " +
                "'${new Date().format('yyyy-MM-dd HH:mm:ss')}', ${cont}, ${rept})"
//        println "sql: $sql"
        cn.execute(sql.toString())
    }

    def archivoProblema(arch, mg) {
        def cn = dbConnectionService.getConnection()
        def sql = "insert into survey.problem(id, rgst, file, datetime, cont) values(default, '${mg}', '${arch}', " +
                "'${new Date().format('yyyy-MM-dd HH:mm:ss')}', 1) " +
                "on conflict (file) do update set datetime = '${new Date().format('yyyy-MM-dd HH:mm:ss')}', " +
                "cont =  survey.problem.cont + 1"
//                println "sql: $sql"
        cn.execute(sql.toString())
    }

    def incrementa(cont) {
        return cont++
    }

    def cambiaEstacion(dsde, hsta, magn) {
        def cn = dbConnectionService.getConnection()
        def sql = "update survey.data set opoint_id = ${hsta} where magnitude_id in (${magn}) and opoint_id = ${dsde}"
        println "sql: $sql"
        cn.execute(sql.toString())
    }



}
