package com.techelevator.ssgeek.dao;

import com.techelevator.ssgeek.exception.DaoException;
import com.techelevator.ssgeek.model.LineItem;
import com.techelevator.ssgeek.model.Sale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JdbcSaleDao implements SaleDao, LineItemDao{

    private final String SALE_SELECT = "SELECT s.sale_id, s.customer_id, s.sale_date, s.ship_date, " +
            "c.name FROM sale AS s " +
            "JOIN customer AS c ON s.customer_id = c.customer_id ";
    private final JdbcTemplate dao;

    public JdbcSaleDao(DataSource dataSource) {
        this.dao = new JdbcTemplate(dataSource);
    }

    @Override
    public Sale getSaleById(int saleId) {
        Sale sale = null;

        String sql = SALE_SELECT + " WHERE s.sale_id = ?;";
        try {
            SqlRowSet results = dao.queryForRowSet(sql, saleId);
            while (results.next()) {
                sale = mapRowToSale(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return sale;
    }

    @Override
    public List<Sale> getUnshippedSales() {
        List<Sale> sales = new ArrayList<>();

        String sql = SALE_SELECT + " WHERE s.ship_date IS NULL " +
                "ORDER BY s.sale_id;";
        try {
            SqlRowSet results = dao.queryForRowSet(sql);
            while (results.next()) {
                Sale sale = new Sale();
                sale = mapRowToSale(results);
                sales.add(sale);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return sales;
    }

    @Override
    public List<Sale> getSalesByCustomerId(int customerId) {
        List<Sale> sales = new ArrayList<>();

        String sql = SALE_SELECT + " WHERE s.customer_id = ? " +
                "ORDER BY sale_id;";
        try {
            SqlRowSet results = dao.queryForRowSet(sql, customerId);
            while (results.next()) {
                Sale sale = new Sale();
                sale = mapRowToSale(results);
                sales.add(sale);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return sales;
    }

    @Override
    public List<Sale> getSalesByProductId(int productId) {
        List<Sale> sales = new ArrayList<>();

        String sql = SALE_SELECT +
                "JOIN line_item AS li ON s.sale_id = li.sale_id " +
                " WHERE li.product_id = ? " +
                "ORDER BY sale_id;";
        try {
            SqlRowSet results = dao.queryForRowSet(sql, productId);
            while (results.next()) {
                Sale sale = new Sale();
                sale = mapRowToSale(results);
                sales.add(sale);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return sales;
    }

    @Override
    public Sale createSale(Sale newSale) {
        Sale sale = null;

        String sql = "INSERT INTO sale (customer_id, sale_date, ship_date) " +
                "VALUES (?, ?, ?) RETURNING sale_id;";
        try {
            int newSaleId = dao.queryForObject(sql, int.class, newSale.getCustomerId(),
                    newSale.getSaleDate(), newSale.getShipDate());

            sale = getSaleById(newSaleId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return sale;
    }

    @Override
    public Sale updateSale(Sale updatedSale) {
        Sale sale = null;

        String sql = "UPDATE sale SET customer_id = ?, sale_date = ?, ship_date = ? " +
                "WHERE sale_id = ?;";
        try {
            int numberOfRows = dao.update(sql, updatedSale.getCustomerId(),
                    updatedSale.getSaleDate(), updatedSale.getShipDate(), updatedSale.getSaleId());

            if (numberOfRows == 0) {
                throw new DaoException("Zero rows affected, expected at least one");
            } else {
                sale = getSaleById(updatedSale.getSaleId());
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return sale;
    }

    @Override
    public int deleteSaleById(int saleId) {
        int numberOfRows = 0;

        String deleteLineItemSql = "DELETE FROM line_item WHERE sale_id = ?;";
        String deleteSaleSql = "DELETE FROM sale WHERE sale_id = ?;";

        try {
            dao.update(deleteLineItemSql, saleId);

            numberOfRows = dao.update(deleteSaleSql, saleId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return numberOfRows;
    }

    @Override
    public List<LineItem> getLineItemsBySaleId(int saleId) {
        List<LineItem> lineItems = new ArrayList<>();

        String sql = "SELECT li.line_item_id, li.sale_id, li.product_id, li.quantity, p.name, p.price " +
                "FROM line_item AS li " +
                "JOIN product AS p ON li.product_id = p.product_id " +
                "WHERE li.sale_id = ? " +
                "ORDER BY li.line_item_id;";
        try {
            SqlRowSet results = dao.queryForRowSet(sql, saleId);
            while (results.next()) {
                LineItem lineItem = new LineItem();
                lineItem.setLineItemId(results.getInt("line_item_id"));
                lineItem.setSaleId(results.getInt("sale_id"));
                lineItem.setProductId(results.getInt("product_id"));
                lineItem.setQuantity(results.getInt("quantity"));
                lineItem.setProductName(results.getString("name"));
                lineItem.setPrice(results.getBigDecimal("price"));
                lineItems.add(lineItem);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return lineItems;
    }

    public Sale mapRowToSale(SqlRowSet results) {
        Sale sale = new Sale();
        sale.setSaleId(results.getInt("sale_id"));
        sale.setCustomerId(results.getInt("customer_id"));
        sale.setSaleDate(results.getDate("sale_date").toLocalDate());
        if (results.getDate("ship_date") != null) {
            sale.setShipDate(results.getDate("ship_date").toLocalDate());
        }
        sale.setCustomerName(results.getString("name"));
        return sale;
    }


}
