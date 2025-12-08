package com.datn.shopdatabase.entity;

import com.datn.shopdatabase.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long inventoryItemId; // FK (optional)
    private Long variantId;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // IMPORT, EXPORT, RESERVE, RELEASE, DEDUCT, ADJUST

    private Integer quantity; // +/- quantity (positive for import/reserve, negative for export/deduct)

    private String note;
}
