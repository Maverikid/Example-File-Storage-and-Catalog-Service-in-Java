package com.techelevator.ssgeek.dao;

import com.techelevator.ssgeek.model.Customer;
import com.techelevator.ssgeek.model.Product;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class JdbcProductDaoTest extends BaseDaoTests{

    private static final Product PRODUCT_1 = new Product(1, "Product 1", "Description 1", BigDecimal.valueOf(9.99), "product-1.png");

    private JdbcProductDao dao;

    @Before
    public void setup() {
        dao = new JdbcProductDao(dataSource);
    }

    @Test
    public void getProductById_with_valid_id_returns_correct_product() {
        Product testProduct = dao.getProductById(1);
        Assert.assertNotNull(testProduct);
        assertProductsMatch(PRODUCT_1, testProduct);
    }

    @Test
    public void getProductById_with_invalid_id_returns_null() {
        Product testProduct = dao.getProductById(-1);
        Assert.assertNull(testProduct);
    }

    @Test
    public void getProducts_returns_list() {
        List<Product> testList = dao.getProducts();
        Assert.assertNotNull(testList);
        Assert.assertEquals(4, testList.size());
    }

    @Test
    public void getProductsWithNoSales_returns_correct_list() {
        List<Product> testList = dao.getProductsWithNoSales();
        Assert.assertNotNull(testList);
        Assert.assertEquals(1, testList.size());
    }

    @Test
    public void createProduct_creates_product() {
        Product newProduct = new Product();
        newProduct.setName("Product 5");
        newProduct.setDescription("Description 5");
        newProduct.setPrice(BigDecimal.valueOf(10.00));
        newProduct.setImageName("product-5.png");

        Product createdProduct = dao.createProduct(newProduct);

        int newId = createdProduct.getProductId();
        Product retrievedProduct = dao.getProductById(newId);
        assertProductsMatch(createdProduct, retrievedProduct);
    }

    @Test
    public void updateProduct_updates_product() {
        Product productToUpdate = dao.getProductById(1);
        productToUpdate.setName("Dork 1");

        Product updatedProduct = dao.updateProduct(productToUpdate);

        Product retrievedProduct = dao.getProductById(1);
        assertProductsMatch(updatedProduct, retrievedProduct);
    }

    @Test
    public void deleteProductById_deletes_product() {
        int rowsAffected = dao.deleteProductById(2);

        Assert.assertEquals(1, rowsAffected);

        Product retrievedProduct = dao.getProductById(2);
        Assert.assertNull(retrievedProduct);
    }

    public void assertProductsMatch(Product expected, Product actual) {
        Assert.assertEquals(expected.getProductId(), actual.getProductId());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getPrice(), actual.getPrice());
        Assert.assertEquals(expected.getDescription(), actual.getDescription());
        Assert.assertEquals(expected.getImageName(), actual.getImageName());
    }
}
