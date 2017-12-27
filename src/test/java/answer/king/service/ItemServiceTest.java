package answer.king.service;

import static answer.king.test.ItemUtils.item;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.refEq;

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

	@InjectMocks
	private ItemService itemService;

	@Test
	public void findAllShouldReturnAllItemsFromRepository() {
		// Given
		given(itemRepository.findAll()).willReturn(newArrayList(item(1L, "itemName", 10.0)));

		// when
		List<Item> items = itemService.getAll();

		// then
		assertThat(items).usingFieldByFieldElementComparator().containsExactly(item(1L, "itemName", 10.0));
	}

	@Test
	public void saveShouldSaveItemToRepositoryAndReturnItemUpdatedWithId() {
		// Given
		Item inputItem = item(null, "itemName", 10.0);
		Item itemUpdatedWithId = item(3L, "itemName", 10.0);

		given(itemRepository.save(refEq(inputItem))).willReturn(itemUpdatedWithId);

		// when
		Item returnedItem = itemService.save(inputItem);

		// then
		assertThat(returnedItem).isEqualToComparingFieldByField(itemUpdatedWithId);
	}
}
