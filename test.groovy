@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.5.0-RC2')

import biz.source_code.base64Coder.Base64Coder

@Grab(group = 'biz.source_code', module = 'base64coder', version = '2010-09-21')

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import org.apache.commons.lang.builder.ReflectionToStringBuilder

/**
 * Write file
 * @param directory
 * @param fileName
 * @param extension
 * @param infoList
 */
public void writeToFile(def directory, def fileName, def extension, def infoList) {

}

/**
 * Get PDF
 * @param invoiceId
 * @return
 */
def savePdf(invoiceId) {
    def http = new HTTPBuilder('https://medefa.billomat.net')
    http.request(GET, JSON) {
        uri.path = "/api/invoices/${invoiceId}/pdf"
        uri.query = ['format': 'json']

        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
        headers.'X-BillomatApiKey' = '4b28556815aeb8f67e3937524b73817d'

        // response handler for a success response code:
        response.success = { resp, json ->
            /*println "yo: ${resp.statusLine}"

            resp.headers.each { h ->
                println " ${h.name} : ${h.value}"
            }*/

            file = new File(json.document.filename)
            def decoded = json.document.base64file.decodeBase64()
            file.delete();
            file << decoded
        }

        // handler for any failure status code:
        response.failure = { resp ->
            println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
        }
    }
}

/**
 * Get invoice ids
 * @param status
 * @return
 */
def getInvoiceIds(status) {
    def http = new HTTPBuilder('https://medefa.billomat.net')
    http.request(GET, JSON) {
        uri.path = '/api/invoices'
        uri.query = ['status': status]

        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
        headers.'X-BillomatApiKey' = '4b28556815aeb8f67e3937524b73817d'
        headers.'Accept' = 'application/json'

        // response handler for a success response code:
        response.success = { resp, json ->
            // println "yo: ${resp.statusLine}"

            ids = [];

            // parse the JSON response object:
            json.invoices.invoice.each {
                ids.push(it.id)
            }

            return ids
        }

        // handler for any failure status code:
        response.failure = { resp ->
            println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
        }
    }
}

def invIds = getInvoiceIds('open')

invIds.each {
    savePdf(it)
}