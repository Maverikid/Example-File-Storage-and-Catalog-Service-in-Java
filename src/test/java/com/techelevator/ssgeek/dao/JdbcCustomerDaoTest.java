package com.techelevator.ssgeek.dao;

import com.techelevator.ssgeek.model.Customer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class JdbcCustomerDaoTest extends BaseDaoTests{

    private static final Customer CUSTOMER_1 = new Customer(1, "Customer 1", "Addr 1-1", "Addr 1-2", "City 1", "S1", "11111");
    private JdbcCustomerDao dao;

    @Before
    public void setup() {
        dao = new JdbcCustomerDao(dataSource);
    }

    @Test
    public void getCustomerById_with_valid_id_returns_correct_customer() {
        Customer testCustomer = dao.getCustomerById(1);
        Assert.assertNotNull(testCustomer);
        assertCustomersMatch(CUSTOMER_1, testCustomer);
    }

    @Test
    public void getCustomerById_with_invalid_id_returns_null() {
        Customer testCustomer = dao.getCustomerById(-1);
        Assert.assertNull(testCustomer);
    }

    @Test
    public void getCustomers_returns_list() {
        List<Customer> testList = dao.getCustomers();
        Assert.assertNotNull(testList);
        Assert.assertEquals(4, testList.size());
    }

    @Test
    public void createCustomer_creates_customer() {
        Customer newCustomer = new Customer();
        newCustomer.setName("Customer 5");
        newCustomer.setStreetAddress1("Addr 5-1");
        newCustomer.setStreetAddress2(null);
        newCustomer.setCity("City 5");
        newCustomer.setState("S5");
        newCustomer.setZipCode("55555");

        Customer createdCustomer = dao.createCustomer(newCustomer);

        int newId = createdCustomer.getCustomerId();
        Customer retrievedCustomer = dao.getCustomerById(newId);
        assertCustomersMatch(createdCustomer, retrievedCustomer);
    }

    @Test
    public void updateCustomer_updates_customer() {
        Customer customerToUpdate = dao.getCustomerById(1);
        customerToUpdate.setName("Dork 1");

        Customer updatedCustomer = dao.updateCustomer(customerToUpdate);

        Customer retrievedCustomer = dao.getCustomerById(1);
        assertCustomersMatch(updatedCustomer, retrievedCustomer);
    }

    private void assertCustomersMatch(Customer expected, Customer actual) {
        Assert.assertEquals(expected.getCustomerId(), actual.getCustomerId());
        Assert.assertEquals(expected.getStreetAddress1(), actual.getStreetAddress1());
        Assert.assertEquals(expected.getStreetAddress2(), actual.getStreetAddress2());
        Assert.assertEquals(expected.getCity(), actual.getCity());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getState(), actual.getState());
        Assert.assertEquals(expected.getZipCode(), actual.getZipCode());
    }

}
