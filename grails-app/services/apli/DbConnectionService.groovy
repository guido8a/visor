package apli

import groovy.sql.Sql
import org.springframework.jdbc.core.JdbcTemplate

class DbConnectionService {
    boolean transactional = false

    def dataSource

    public init(){
    }

    /**
     * Devuelve la conexión a la base de datos
     */
    def getConnection(){

        Sql sql = new Sql(dataSource)
        return sql
    }

    def ejecutarProcedure(nombre, parametros,condiciones) {
        def sql = " select " + nombre + "(" + parametros + ")"+condiciones
//        println "ejecutar Procedure " + sql
        def template = new JdbcTemplate(dataSource)
        def result = template.queryForMap(sql)
//        println "result " + result
        return result
    }

    def ejecutar (sql) {
        def template = new JdbcTemplate(dataSource)
        def result = template.queryForMap(sql)
        return result
    }
}
