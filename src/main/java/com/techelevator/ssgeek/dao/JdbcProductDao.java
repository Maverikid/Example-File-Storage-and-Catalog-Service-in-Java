package com.techelevator.ssgeek.dao;

import com.techelevator.ssgeek.exception.DaoException;
import com.techelevator.ssgeek.model.Product;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class JdbcProductDao implements ProductDao {

    private final JdbcTemplate dao;

    public JdbcProductDao(DataSource dataSource) {
        this.dao = new JdbcTemplate(dataSource);
    }

    @Override
    public Product getProductById(int productId) {
        Product product = null;

        String sql = "SELECT product_id, name, description, " +
                "price, image_name " +
                "FROM product " +
                "WHERE product_id = ?;";
        try {
            SqlRowSet results = dao.queryForRowSet(sql, productId);
            while (results.next()) {
                product = mapRowToProduct(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return product;
    }

    @Override
    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT product_id, name, description, " +
                "price, image_name " +
                "FROM product " +
                "ORDER BY product_id;";
        try {
            SqlRowSet results = dao.queryForRowSet(sql);
            while (results.next()) {
                Product product = new Product();
                product = mapRowToProduct(results);
                products.add(product);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return products;
    }

    @Override
    public List<Product> getProductsWithNoSales() {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT p.product_id, p.name, p.description, " +
                "p.price, p.image_name " +
                "FROM product AS p " +
                "LEFT JOIN line_item AS li ON p.product_id = li.product_id " +
                "WHERE li.product_id IS NULL " +
                "ORDER BY p.product_id;";
        try {
            SqlRowSet results = dao.queryForRowSet(sql);
            while (results.next()) {
                Product product = new Product();
                product = mapRowToProduct(results);
                products.add(product);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return products;
    }

    @Override
    public Product createProduct(Product newProduct) {
        Product product = null;

        String sql = "INSERT INTO product " +
                "(name, description, price, image_name) " +
                "VALUES (?, ?, ?, ?) RETURNING product_id;";
        try {
            int newProductId = dao.queryForObject(sql, int.class, newProduct.getName(),
                    newProduct.getDescription(), newProduct.getPrice(),
                    newProduct.getImageName());

            product = getProductById(newProductId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return product;
    }

    @Override
    public Product updateProduct(Product updatedProduct) {
        Product product = null;

        String sql = "UPDATE product SET name = ?, description = ?, price = ?, " +
                "image_name = ? " +
                "WHERE product_id = ?;";

        try {
            int numberOfRows = dao.update(sql, updatedProduct.getName(), updatedProduct.getDescription(),
                    updatedProduct.getPrice(), updatedProduct.getImageName(), updatedProduct.getProductId());

            if (numberOfRows == 0) {
                throw new DaoException("Zero rows affected, expected at least one");
            } else {
                product = getProductById(updatedProduct.getProductId());
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return product;
    }

    @Override
    public int deleteProductById(int productId) {
        int numberOfRows = 0;

        String deleteLineItemSql = "DELETE FROM line_item WHERE product_id = ?;";
        String deleteProductSql = "DELETE FROM product WHERE product_id = ?;";

        try {
            dao.update(deleteLineItemSql, productId);

            numberOfRows = dao.update(deleteProductSql, productId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return numberOfRows;
    }

    public Product mapRowToProduct(SqlRowSet results) {
        Product product = new Product();
        product.setProductId(results.getInt("product_id"));
        product.setName(results.getString("name"));
        product.setDescription(results.getString("description"));
        product.setPrice(results.getBigDecimal("price"));
        if (results.getString("image_name") != null) {
            product.setImageName(results.getString("image_name"));
        }
        return product;
    }
}
