import groovyx.net.http.*
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.5.0-RC2')
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import org.apache.commons.lang.builder.ReflectionToStringBuilder

def http = new HTTPBuilder('https://medefa.billomat.net')

// perform a GET request, expecting JSON response data
http.request(GET, JSON) {
    uri.path = '/api/invoices'
    uri.query = ['status': 'open']

    headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
    headers.'X-BillomatApiKey' = '4b28556815aeb8f67e3937524b73817d'
    headers.'Accept' = 'application/json'

    // response handler for a success response code:
    response.success = { resp, json ->
        println "yo: ${resp.statusLine}"

        // parse the JSON response object:
        json.invoices.invoice.each {
            println "id: ${it.id}"
        }
    }

    // handler for any failure status code:
    response.failure = { resp ->
        println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
    }
}