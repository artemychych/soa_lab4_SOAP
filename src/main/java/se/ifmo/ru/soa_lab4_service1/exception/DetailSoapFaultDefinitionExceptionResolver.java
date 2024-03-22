package se.ifmo.ru.soa_lab4_service1.exception;

import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.client.WebServiceFaultException;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

import javax.xml.namespace.QName;

public class DetailSoapFaultDefinitionExceptionResolver extends SoapFaultMappingExceptionResolver {
    private static final QName CODE = new QName("code");
    private static final QName DESCRIPTION = new QName("description");

    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
        logger.warn("Exception processed ", ex);
        if (ex instanceof WebServiceFaultException) {
            FaultAwareWebServiceMessage serviceFault = ((WebServiceFaultException) ex).getWebServiceMessage();
            SoapFaultDetail detail = fault.addFaultDetail();
            detail.addFaultDetailElement(CODE).addText(serviceFault.getFaultCode().toString());
            detail.addFaultDetailElement(DESCRIPTION).addText(serviceFault.getFaultReason());
        }
    }
}
