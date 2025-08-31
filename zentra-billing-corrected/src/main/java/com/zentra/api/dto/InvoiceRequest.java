package com.zentra.api.dto;

import com.zentra.api.entity.Invoice;
import com.zentra.api.entity.InvoiceItem;
import java.util.List;

public class InvoiceRequest {
    private Invoice invoice;
    private List<InvoiceItem> items;

    // Getters and Setters
    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
    }
}