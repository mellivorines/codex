package io.github.mellivorines.codex.service.impl

import com.zaxxer.hikari.HikariDataSource
import io.github.mellivorines.codex.constants.CommonConstant
import io.github.mellivorines.codex.model.database.Table
import io.github.mellivorines.codex.model.database.TableField
import io.github.mellivorines.codex.service.DatabaseService
import io.github.mellivorines.codex.utils.StringUtils.sneak2camel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.text.SimpleDateFormat
import java.util.*
import javax.sql.DataSource

@Suppress("NAME_SHADOWING")
@Service
class DatabaseService : DatabaseService {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)


    @Autowired
    lateinit var dataSource: DataSource

    /**
     * 获取数据库全部表
     */
    override fun getAllTables(): List<Table>? {
        val databaseName = getDatabaseName(dataSource)
        val schema: String? = getSchema((dataSource as HikariDataSource).driverClassName)
        val connection = getConnection(dataSource) ?: return null
        val result = ArrayList<Table>()
        var resultSet: ResultSet? = null
        try {
            connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
            val meta = connection.metaData
            //目录名称, 数据库名, 表名称, 表类型
            resultSet = meta.getTables(catalog(databaseName), schema, tableNamePattern(), types())
            while (resultSet?.next()!!) {
                val table = Table(
                    resultSet.getString(CommonConstant.TABLE_NAME),
                    resultSet.getString(CommonConstant.REMARKS),
                    resultSet.getString(CommonConstant.TABLE_NAME).sneak2camel(),
                    SimpleDateFormat("yyyy-MM-dd").format(Date()),
                    null
                )
                result.add(table)
            }
        } catch (e: SQLException) {
            logger.error("获取数据库全部表:", e)
        } finally {
            close(connection, null, resultSet)
        }
        for (table in result) {
            val tableFields = getTableFields(table.tableName)
            table.tableField = tableFields
        }
        return result
    }

    private fun getSchema(driverClassName: String?): String? {
        return if (driverClassName.equals("org.postgresql.Driver")) CommonConstant.PUBLIC else null

    }

    /**
     * 获取数据库表所包含的字段
     */
    override fun getTableFields(tableName: String): List<TableField>? {
        val databaseName = getDatabaseName(dataSource)
        val schema: String? = this.getSchema((dataSource as HikariDataSource).driverClassName)
        val connection = getConnection(dataSource) ?: return null
        val result = ArrayList<TableField>()
        var resultSet: ResultSet? = null
        try {
            val meta = connection.metaData
            resultSet = meta.getColumns(catalog(databaseName), schema, tableName, null)
            while (resultSet.next()) {
                val tableField = TableField(
                    resultSet.getString(CommonConstant.TABLE_NAME),
                    resultSet.getString(CommonConstant.COLUMN_NAME),
                    resultSet.getString(CommonConstant.REMARKS),
                    resultSet.getString(CommonConstant.TYPE_NAME).lowercase(),
                    null,
                    resultSet.getString(CommonConstant.COLUMN_NAME).sneak2camel(true),
                    null,
                    resultSet.getInt(CommonConstant.COLUMN_SIZE)
                )
                result.add(tableField)
            }
        } catch (e: Exception) {
            logger.error("获取数据库表所包含的字段：", e)
        } finally {
            close(connection, null, resultSet)
        }
        return result
    }

    /**
     * a catalog name;
     * must match the catalog name as it is stored in the service;
     * "" retrieves those without a catalog; null means that the catalog name should not be used to narrow the search
     */
    fun catalog(databaseName: String): String? {
        return databaseName
    }

    /**
     * a table name pattern;
     * must match the table name as it is stored in the service
     */
    fun tableNamePattern(): String {
        return "%"
    }

    /**
     * a list of table types,
     * which must be from the list of table types returned from DatabaseMetaData,
     * to include; null returns all types
     */
    fun types(): Array<String> {
        return arrayOf(CommonConstant.TABLE, CommonConstant.VIEW)
    }

    /**
     * 获取数据库名称
     */
    fun getDatabaseName(dataSource: DataSource): String =
        (dataSource as HikariDataSource).jdbcUrl.substringBefore("?").substringAfterLast("/")

    override fun getConnection(dataSource: DataSource): Connection? {
        var connection: Connection? = null
        try {
            connection = dataSource.connection
        } catch (e: SQLException) {
            logger.error("数据库连接失败！", e)
        }
        return connection
    }

    override fun close(connection: Connection?, ps: Statement?, rs: ResultSet?) {
        @Suppress("NAME_SHADOWING") val connection = connection
        //关闭ResultSet
        if (rs != null) {
            try {
                rs.close()
            } catch (e: SQLException) {
                logger.error(e.message)
            }
        }
        //关闭PreparedStatement
        if (ps != null) {
            try {
                ps.close()
            } catch (e: SQLException) {
                logger.error(e.message)
            }
        }
        //关闭Connection
        if (connection != null) {
            try {
                connection.close()
            } catch (e: SQLException) {
                logger.error(e.message)
            }
        }

    }

    /**
     * 生成 DDL 语句
     */
    fun generateDDL(table: String): String? {
        val fields = getTableFields(table)
        return ddl(table, fields)
    }

    fun ddl(table: String, fields: List<TableField>?): String {
        val fieldLines = StringBuilder()
        fields?.forEachIndexed { index, fieldInfo ->
            if (index == 0) {
                val line = "${fieldInfo.fieldName}               STRING COMMENT '${fieldInfo.fieldComment}'"
                fieldLines.append("\n")
                fieldLines.append(line)
                fieldLines.append("\n")
            } else {
                val line = ",${fieldInfo.fieldName}               STRING COMMENT '${fieldInfo.fieldComment}'"
                fieldLines.append(line)
                fieldLines.append("\n")
            }
        }
        return """
                CREATE TABLE IF NOT EXISTS $table(
                $fieldLines 
                )
                COMMENT '' PARTITIONED BY
                (
                  pt STRING COMMENT '时间分区键-yyyymmdd'
                )
                LIFECYCLE 750;
                """.trimIndent()
    }

}
