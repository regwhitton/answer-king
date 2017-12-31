package answer.king.test;

import java.math.BigDecimal;

import answer.king.model.Item;

public class TestUtils {

	private TestUtils() {
	}

	public static Item item(Long id, String name, Double price) {
		Item item = new Item();
		item.setId(id);
		item.setName(name);
		item.setPrice(price == null ? null : BigDecimal.valueOf(price));
		return item;
	}
}
