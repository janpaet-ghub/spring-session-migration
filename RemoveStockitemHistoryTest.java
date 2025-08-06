package de.fms.scm.persistence.scmwhtd;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import category.DatamodelCategory;
import de.fms.scm.service.AbstractServiceIT;
import de.fms.scm.service.warehouse.WarehouseService;

/**
 * SCM-2017
 */

//@Transactional
@Rollback(false)
@Category(DatamodelCategory.class)
public class RemoveStockitemHistoryTest extends AbstractServiceIT {

	@Autowired
	private WarehouseService warehouseService;
	
	@Test
	public void testRemoveStockitemHistoryTest() {
		Assert.assertTrue(true);
		/*
		final String warehouseStatus = "OUTBOUND";
		final String modificationDate = "2024-06-15 15:34:08.215";
		warehouseService.removeStockitemHistory(warehouseStatus, modificationDate);
		*/
	}
}
