package visor

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat

class VisorJob {

    def lecturasService
    def dbConnectionService

    static triggers = {
        simple startDelay: 1000*60*1, repeatInterval: 1000*60*60*8  /* cada 10 minutos */
//        simple startDelay: 1000*3, repeatInterval: 1000*60*60*50  /* cada 10 minutos */
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
        def cont = 1
        println ">>> Ejecuta procesos automáticos: ${new Date()}"
        lecturasService.mueveArch()
        println ">>> Inicia cargado de datos de archivos en ../data: ${new Date()}"
//        cargaArchivo('prueba')
        cargaArchivo('prod')
        lecturasService.calcular()
        lecturasService.calcularDir()

        println "Fin procesos automáticos: ${new Date()}"
//        lecturasService.leeCSV('prueba')  /* no se usa */
//        lecturasService.leeCSV('prod')  /* no se usa */


    }


    def cargaArchivo(tipo) {
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
                    print "Cargando archivo: $ar "   /** menaje de cargado de archivo **/
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
                                estc = lecturasService.datosEstaciones(rgst)
//                                if(estc && cuenta == 0) cuenta = 1
//                                println "estaciones: $estc"
                            } else if (cuenta ==1) {
//                                println ">>cuenta: $cuenta, registro: $rgst"
                                if(rgst[1].toString().contains('Ed')) {
                                    rgst = rgst*.replaceAll(('EdPAR'), ('PAR'))
                                }
                                mg = rgst[1..-1]
//                                println "buisca magn: $mg"
                                magn = lecturasService.buscaMagnIUV(mg)
//                                println "-----> mag: ${magn[0]} ---> $rgst"
                                if(magn[0] == null) {
                                    prob_magn += "\n${mg}"
                                    lecturasService.archivoProblema(arch, mg)
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
                                    inserta = lecturasService.cargarLectIUV(rgst, magn, estc)
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
                        lecturasService.archivoSubido(arch, cont, repetidos)
                    }
                    println "--> cont: $cont, repetidos: $repetidos"   /** menaje de cargado de archivo **/
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




}