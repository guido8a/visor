package visor

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class VisorJob {

    def lecturasService

    static triggers = {
//        simple startDelay: 1000*60*1, repeatInterval: 1000*60*60*5  /* cada 10 minutos */
        simple startDelay: 1000*3, repeatInterval: 1000*60*60*50  /* cada 10 minutos */
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
        println "Ejecuta procesos automáticos: ${new Date()}"
        lecturasService.mueveArch()
//        lecturasService.cargaIUV('prueba')
//        lecturasService.cargaIUV('prod')
//        lecturasService.calcular()
//        lecturasService.calcularDir()

        println "Fin procesos automáticos: ${new Date()}"
//        lecturasService.leeCSV('prueba')  /* no se usa */
//        lecturasService.leeCSV('prod')  /* no se usa */


    }





}