package answer.king.controller;

import static answer.king.test.TestUtils.*;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import answer.king.model.Item;
import answer.king.service.InvalidItemException;
import answer.king.service.ItemService;

@RunWith(SpringRunner.class)
@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {

	@MockBean
	private ItemService itemService;

	@Autowired
	private MockMvc mvc;

	@Test
	public void getShouldGetAllItemsFromItemService() throws Exception {
		// Given
		given(itemService.getAll()).willReturn(newArrayList(item(1L, "itemName", 10.0)));

		// when & then
		mvc.perform(get("/item").accept(APPLICATION_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(content().json("[{'id':1, 'name':'itemName', 'price':10}]"));
	}

	@Test
	public void postShouldCreateItemUsingItemService() throws Exception {
		// Given
		Item inputItem = item(null, "itemName", 10.0);
		Item itemUpdatedWithId = item(3L, "itemName", 10.0);

		given(itemService.save(refEq(inputItem))).willReturn(itemUpdatedWithId);

		// when & then
		mvc.perform( //
				post("/item").contentType(APPLICATION_JSON).content("{\"name\":\"itemName\",\"price\":10}")
						.accept(APPLICATION_JSON))
				.andExpect(status().isOk()) //
				.andExpect(content().json("{'id':3, 'name':'itemName', 'price':10}"));
	}

	@Test
	public void postShouldReturn400WhenItemIsInvalid() throws Exception {
		// Given
		Item inputItem = item(null, null, 10.0);
		given(itemService.save(refEq(inputItem))).willThrow(new InvalidItemException("item name must be provided"));

		// when & then
		mvc.perform( //
				post("/item").contentType(APPLICATION_JSON).content("{\"price\":10}").accept(APPLICATION_JSON))
				.andExpect(status().isBadRequest()) //
				.andExpect(content().json("{'error':'item name must be provided'}"));
	}
}