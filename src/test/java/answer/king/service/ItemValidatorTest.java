package answer.king.service;

import static answer.king.test.TestUtils.item;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ItemValidatorTest {

	@InjectMocks
	private ItemValidator itemValidator;

	@Test
	public void shouldPassItemWithValidFields() throws Exception {
		itemValidator.validate(item(null, "itemName", 10.0));
	}

	@Test(expected = InvalidItemException.class)
	public void shouldFailIfItemNotProvided() throws Exception {
		itemValidator.validate(null);
	}

	@Test(expected = InvalidItemException.class)
	public void shouldFailIfNameNotProvided() throws Exception {
		itemValidator.validate(item(null, null, 10.0));
	}

	@Test(expected = InvalidItemException.class)
	public void shouldFailIfNameIsBlank() throws Exception {
		itemValidator.validate(item(null, "", 10.0));
	}

	@Test(expected = InvalidItemException.class)
	public void shouldFailIfPriceNotProvided() throws Exception {
		itemValidator.validate(item(null, "itemName", null));
	}

	@Test(expected = InvalidItemException.class)
	public void shouldFailIfPriceIsNegative() throws Exception {
		itemValidator.validate(item(null, "itemName", -1.0));
	}
}
