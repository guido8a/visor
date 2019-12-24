package visor

import org.apache.tomcat.util.http.fileupload.FileUtils
import org.omg.CORBA.Environment

import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.nio.file.Files

class DatosController {
    def dbConnectionService
    def lecturasService

    def index() {
        redirect(controllerName: 'datos', action: 'cargarDatos')
    }

    def cargarDatos() {
        def cn = dbConnectionService.getConnection()
        def sql = ""
        def data = []

        sql = "select id magn__id, pname||'('||abbreviation||')' nombre from survey.magnitude order by pname"
        data = cn.rows(sql.toString())

        [magnitud: data]
    }


    def leeCSV() {
        println "datos---- leeCSV"
        def tipo = params.tipo
        def cargar = lecturasService.leeCSV(tipo)
        flash.message = cargar
        render "ok"
    }


    def calcular() {
        println "datos---- calcular"
        def cargar = lecturasService.calcular()
        flash.message = cargar
        render "ok"
    }


    def calcularDir() {
        println "datos ---- calcularDir"
        def cargar = lecturasService.calcularDir()
        flash.message = cargar
        render "ok"
    }

    def cargaIUV() {
        println "datos --- cargaIUV.."
        def cargar = lecturasService.cargaIUV()
        flash.message = cargar
        render "ok"
    }

    def mueveArch() {
        println "data --- mueveArch.."
        def cargar = lecturasService.mueveArch()
        flash.message = cargar
        render "ok"
    }





    def cargarMinutos(vrbl, estc, rgst) {
        def errores = ""
        def cnta = 0
        def insertados = 0
        def repetidos = 0
        def fcha
        def cn = dbConnectionService.getConnection()
        def sql = ""

//        println "inicia cargado de datos para mag: $vrbl, .... $rgst"
        fcha = rgst[0]
        rgst.removeAt(0)  // elimina la fecha y quedan solo lecturas

        cnta = 0
        rgst.each() { rg ->
//            println "--> estación: ${estc[cnta]}, valor: $rg, tipo: ${rg.class}, ${rg.size()}"
            if (rg.toString().size() > 0) {
//                println "--> estación: ${estc[cnta]}, valor: $rg"
                sql = "insert into mnto(lctr__id, magn__id, estc__id, lctrvlor, lctrfcha, lctrvlda) " +
                        "values(default, ${vrbl}, ${estc[cnta]}, ${rg.toDouble()}, '${fcha.format('yyyy-MM-dd HH:mm')}', 'V')"
                println "sql: $sql"
                try {
//                    println "inserta: $inserta"
                    cn.execute(sql.toString())
                    insertados++
/*
                    if(cn.execute(sql.toString()) > 0){
                        cnta++
                    }
*/
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







}
