package com.shopping.shoppinglist.repository;

import com.shopping.shoppinglist.model.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
    List<ShoppingItem> findByShoppingListId(Long shoppingListId);
}
