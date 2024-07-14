package com.techelevator.ssgeek.dao;

import com.techelevator.ssgeek.model.LineItem;
import com.techelevator.ssgeek.model.Product;
import com.techelevator.ssgeek.model.Sale;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class JdbcSaleDaoTest extends BaseDaoTests{

    private static final Sale SALE_1 = new Sale(1, 1, LocalDate.of(2022, 1, 1), null, "Customer 1");

    private JdbcSaleDao dao;

    @Before
    public void setup() {
        dao = new JdbcSaleDao(dataSource);
    }

    @Test
    public void getSaleById_with_valid_id_returns_correct_sale() {
        Sale testSale = dao.getSaleById(1);
        Assert.assertNotNull(testSale);
        assertSalesMatch(SALE_1, testSale);
    }

    @Test
    public void getSaleById_with_invalid_id_returns_null() {
        Sale testSale = dao.getSaleById(-1);
        Assert.assertNull(testSale);
    }

    @Test
    public void getUnshippedSales_returns_correct_list_size() {
        List<Sale> sales = dao.getUnshippedSales();
        Assert.assertNotNull(sales);
        Assert.assertEquals(2, sales.size());
    }

    @Test
    public void getSalesByCustomerId_returns_correct_list_size() {
        List<Sale> sales = dao.getSalesByCustomerId(1);
        Assert.assertNotNull(sales);
        Assert.assertEquals(2, sales.size());
    }

    @Test
    public void getSalesByProductId_returns_correct_list_size() {
        List<Sale> sales = dao.getSalesByProductId(1);
        Assert.assertNotNull(sales);
        Assert.assertEquals(3, sales.size());
    }

    @Test
    public void createSale_creates_sale() {
        Sale newSale = new Sale();
        newSale.setCustomerId(1);
        newSale.setCustomerName("Customer 1");
        newSale.setSaleDate(LocalDate.of(2022, 1, 1));

        Sale createdSale = dao.createSale(newSale);

        int newId = createdSale.getSaleId();
        Sale retrievedSale = dao.getSaleById(newId);
        assertSalesMatch(createdSale, retrievedSale);
    }

    @Test
    public void updateSale_updates_sale() {
        Sale saleToUpdate = dao.getSaleById(1);
        saleToUpdate.setSaleDate(LocalDate.of(2022, 2, 2));

        Sale updatedSale = dao.updateSale(saleToUpdate);

        Sale retrievedSale = dao.getSaleById(1);
        assertSalesMatch(updatedSale, retrievedSale);
    }

    @Test
    public void deleteSaleById_deletes_sale() {
        int rowsAffected = dao.deleteSaleById(2);

        Assert.assertEquals(1, rowsAffected);

        Sale retrievedSale = dao.getSaleById(2);
        Assert.assertNull(retrievedSale);
    }

    @Test
    public void getLineItemsBySaleId_returns_correct_list_size() {
        List<LineItem> lineItems = dao.getLineItemsBySaleId(1);
        Assert.assertNotNull(lineItems);
        Assert.assertEquals(3, lineItems.size());
    }

    public void assertSalesMatch(Sale expected, Sale actual) {
        Assert.assertEquals(expected.getSaleId(), actual.getSaleId());
        Assert.assertEquals(expected.getSaleDate(), actual.getSaleDate());
        Assert.assertEquals(expected.getShipDate(), actual.getShipDate());
        Assert.assertEquals(expected.getCustomerId(), actual.getCustomerId());
        Assert.assertEquals(expected.getCustomerName(), actual.getCustomerName());
    }

}
