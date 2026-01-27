package com.shopping.shoppinglist.repository;

import com.shopping.shoppinglist.model.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    List<ShoppingList> findByWorkspaceCode(String workspaceCode);
}
