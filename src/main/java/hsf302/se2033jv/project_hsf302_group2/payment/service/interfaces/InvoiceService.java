package hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.payment.dto.response.InvoiceResponse;

public interface InvoiceService {

    InvoiceResponse getInvoice(Integer orderId);

    void resendInvoiceEmail(Integer orderId);
}
