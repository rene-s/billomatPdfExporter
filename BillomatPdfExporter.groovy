@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.5.2')

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

/**
 * Exports Billomat invoices as PDF
 */
class PdfExporter {

    /**
     * Invoice IDs
     */
    private ids = []

    /**
     * HTTPBuilder
     */
    private http

    /**
     * Constructor
     */
    PdfExporter() {
        this.http = new HTTPBuilder('https://medefa.billomat.net')
        this.http.setHeaders([
                'User-Agent': 'BilloPdfExporter 0.1',
                'X-BillomatApiKey': '4b28556815aeb8f67e3937524b73817d',
                'Accept': 'application/json'
        ])
    }

    /**
     * Save PDF
     * @param invoiceId
     * @return
     */
    public void savePdf(invoiceId) {
        this.http.request(GET, JSON) {
            uri.path = "/api/invoices/${invoiceId}/pdf"
            uri.query = ['format': 'json']

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

    /**
     * Get invoice ids
     * @param status
     * @return
     */
    public List getInvoiceIds(status) {
        this.http.request(GET, JSON) {
            uri.path = '/api/invoices'
            uri.query = ['status': status]

            response.success = { resp, json ->
                ids = []

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

    /**
     *
     * @param arguments
     */
    static main(arguments) {
        println "Starting export..."

        def exporter = new PdfExporter()
        def invIds = exporter.getInvoiceIds('open')

        invIds.each { invoiceId ->
            exporter.savePdf(invoiceId)
        }

        println "done."
    }
}
