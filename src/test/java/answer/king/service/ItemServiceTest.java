package answer.king.service;

import static answer.king.test.TestUtils.item;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import answer.king.model.Item;
import answer.king.repo.ItemRepository;

@RunWith(MockitoJUnitRunner.class)
public class ItemServiceTest {

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private ItemValidator itemValidator;

	@InjectMocks
	private ItemService itemService;

	@Test
	public void findAllShouldReturnAllItemsFromRepository() {
		// Given
		given(itemRepository.findAll()).willReturn(newArrayList(item(1010L, "itemName", 10.0)));

		// when
		List<Item> items = itemService.getAll();

		// then
		assertThat(items).usingFieldByFieldElementComparator().containsExactly(item(1010L, "itemName", 10.0));
	}

	@Test
	public void saveShouldSaveItemToRepositoryAndReturnItemUpdatedWithId() throws Exception {
		// Given
		Item inputItem = item(null, "itemName", 10.0);
		Item itemUpdatedWithId = item(3030L, "itemName", 10.0);

		given(itemRepository.save(refEq(inputItem))).willReturn(itemUpdatedWithId);

		// when
		Item returnedItem = itemService.save(inputItem);

		// then
		assertThat(returnedItem).isEqualToComparingFieldByField(itemUpdatedWithId);
	}

	@Test(expected = InvalidItemException.class)
	public void saveShouldFailWhenItemIsInvalid() throws Exception {
		// Given
		Item inputItem = item(null, null, 10.0);
		doThrow(new InvalidItemException("")).when(itemValidator).validate(refEq(inputItem));

		// when
		itemService.save(inputItem);
	}

	@Test
	public void updatePriceShouldUpdateItemInRepositoryAndReturnIt() throws Exception {
		// Given
		long itemId = 303L;
		Item originalItem = item(itemId, "itemName", 10.0);
		double newPrice = 15.0;
		Item expectedItem = item(itemId, "itemName", newPrice);

		given(itemRepository.findOne(eq(itemId))).willReturn(originalItem);
		given(itemRepository.save(refEq(expectedItem))).willReturn(expectedItem);

		// when
		Item returnedItem = itemService.updatePrice(itemId, BigDecimal.valueOf(newPrice));

		// then
		then(itemRepository).should().save(refEq(expectedItem));
		assertThat(returnedItem).isEqualToComparingFieldByField(expectedItem);
	}

	@Test(expected = InvalidItemException.class)
	public void updatePriceShouldFailWhenPriceIsInvalid() throws Exception {
		long itemId = 3030L;
		double invalidPrice = -1.0;

		given(itemRepository.findOne(eq(itemId))).willReturn(item(itemId, "itemName", 10.0));
		doThrow(new InvalidItemException("invalid price")).when(itemValidator)
				.validate(refEq(item(itemId, "itemName", invalidPrice)));

		// when
		itemService.updatePrice(itemId, BigDecimal.valueOf(invalidPrice));

		// then exception
	}
}
