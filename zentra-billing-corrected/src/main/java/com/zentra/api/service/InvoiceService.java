package com.zentra.api.service;

import com.zentra.api.entity.Invoice;
import com.zentra.api.entity.InvoiceItem;
import com.zentra.api.repository.InvoiceItemRepository;
import com.zentra.api.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ProductService productService; // Injected ProductService

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceItemRepository invoiceItemRepository, ProductService productService) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.productService = productService; // Added to constructor
    }

    public Page<Invoice> getAllInvoices(Pageable pageable) {
        log.info("Fetching all invoices for page: {}", pageable.getPageNumber());
        return invoiceRepository.findAll(pageable);
    }

    public Optional<Invoice> getInvoiceById(Long id) {
        log.info("Fetching invoice with ID: {}", id);
        return invoiceRepository.findById(id);
    }

    @Transactional
    public Invoice createInvoice(Invoice invoice, List<InvoiceItem> items) {
        log.info("Creating a new invoice");
        prepareInvoiceForSave(invoice);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        for (InvoiceItem item : items) {
            item.setInvoiceId(savedInvoice.getId());
            invoiceItemRepository.save(item);
            // **FIX: Use the injected productService to update stock**
            productService.updateStock(item.getProductId(), item.getQuantity());
        }
        log.info("Successfully created invoice with ID: {}", savedInvoice.getId());
        return savedInvoice;
    }
    
    @Transactional
    public Optional<Invoice> updateInvoice(Long id, Invoice invoiceDetails, List<InvoiceItem> items) {
        log.info("Attempting to update invoice with ID: {}", id);
        return invoiceRepository.findById(id).map(existingInvoice -> {
            
            // Return old items to stock
            List<InvoiceItem> oldItems = invoiceItemRepository.findByInvoiceId(id);
            for (InvoiceItem oldItem : oldItems) {
                productService.updateStock(oldItem.getProductId(), -oldItem.getQuantity()); // Add stock back
            }

            existingInvoice.setCustomerId(invoiceDetails.getCustomerId());
            existingInvoice.setDueDate(invoiceDetails.getDueDate());
            // ... copy other fields ...
            
            prepareInvoiceForSave(existingInvoice);

            invoiceItemRepository.deleteByInvoiceId(id);
            for (InvoiceItem item : items) {
                item.setInvoiceId(existingInvoice.getId());
                invoiceItemRepository.save(item);
                productService.updateStock(item.getProductId(), item.getQuantity()); // Deduct new stock
            }
            log.info("Successfully updated invoice with ID: {}", id);
            return existingInvoice;
        });
    }

    @Transactional
    public void deleteInvoice(Long id) {
        log.info("Deleting invoice with ID: {}", id);
        if (invoiceRepository.existsById(id)) {
            // Return items to stock before deleting
            List<InvoiceItem> itemsToDelete = invoiceItemRepository.findByInvoiceId(id);
            for (InvoiceItem item : itemsToDelete) {
                productService.updateStock(item.getProductId(), -item.getQuantity()); // Add stock back
            }

            invoiceItemRepository.deleteByInvoiceId(id);
            invoiceRepository.deleteById(id);
            log.info("Successfully deleted invoice with ID: {}", id);
        } else {
            log.warn("Attempted to delete non-existent invoice with ID: {}", id);
        }
    }

    @Transactional
    public Optional<Invoice> updatePaymentStatus(Long id, String status) {
        log.info("Updating payment status for invoice ID: {} to {}", id, status);
        return invoiceRepository.findById(id).map(invoice -> {
            try {
                Invoice.PaymentStatus paymentStatus = Invoice.PaymentStatus.valueOf(status.toUpperCase());
                invoice.setPaymentStatus(paymentStatus);
                if (paymentStatus == Invoice.PaymentStatus.PAID) {
                    invoice.setPaidAt(LocalDateTime.now());
                }
                invoice.setUpdatedAt(LocalDateTime.now());
                log.info("Status updated successfully for invoice ID: {}", id);
                return invoiceRepository.save(invoice);
            } catch (IllegalArgumentException e) {
                log.error("Invalid payment status provided: {}", status, e);
                return null; 
            }
        }).flatMap(Optional::ofNullable);
    }

    private void prepareInvoiceForSave(Invoice invoice) {
        invoice.setUpdatedAt(LocalDateTime.now());
        if (invoice.getId() == null) {
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setInvoiceDate(LocalDate.now());
            invoice.setInvoiceNumber(generateUniqueInvoiceNumber());
        }
    }
    
    private String generateUniqueInvoiceNumber() {
        String invoiceNumber;
        do {
            invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (invoiceRepository.findByInvoiceNumber(invoiceNumber).isPresent());
        return invoiceNumber;
    }
}

