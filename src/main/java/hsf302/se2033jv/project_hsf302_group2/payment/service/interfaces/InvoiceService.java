package hsf302.se2033jv.project_hsf302_group2.payment.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.payment.dto.response.InvoiceListItemResponse;
import hsf302.se2033jv.project_hsf302_group2.payment.dto.response.InvoiceResponse;

import java.util.List;

public interface InvoiceService {

    List<InvoiceListItemResponse> getInvoiceList();

    InvoiceResponse getInvoice(Integer orderId);

    void resendInvoiceEmail(Integer orderId);
}
