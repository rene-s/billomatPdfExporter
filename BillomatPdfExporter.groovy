@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.5.2')

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class PdfExporter {

    /**
     * Save PDF
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

            response.success = { resp, json ->
                def file = new File(json.document.filename)
                file.delete();
                file << json.document.base64file.decodeBase64()
            }

            response.failure = { resp ->
                println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
            }
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

        response.success = { resp, json ->
            ids = [];

            json.invoices.invoice.each { invoice ->
                ids.push(invoice.id)
            }

            return ids
        }

        response.failure = { resp ->
            println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
        }
    }
}


def billomat = new PdfExporter()

def invIds = getInvoiceIds('open')

invIds.each { invoiceId ->
    billomat.savePdf(invoiceId)
}