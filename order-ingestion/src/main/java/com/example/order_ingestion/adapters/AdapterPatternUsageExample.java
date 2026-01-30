package com.example.order_ingestion.adapters;

/**
 * Example usage demonstrating the Adapter Pattern implementation.
 *
 * This shows how the new architecture eliminates duplication and improves scalability.
 *
 * BEFORE (Old Approach - 2 mapper methods with duplication):
 * ============================================================
 * OrderEvent eventA = orderMapper.toEntity(partnerAOrder);  // 11 @Mapping annotations
 * OrderEvent eventB = orderMapper.toEntity(partnerBOrder);  // 11 @Mapping annotations (duplicated!)
 *
 * Problems:
 * - 22 lines of mapping annotations (11 per partner)
 * - Adding Partner C means +11 more annotations
 * - All partner-specific logic mixed in one mapper
 * - Hard to test partner-specific logic in isolation
 *
 *
 * AFTER (New Approach - Adapter Pattern):
 * =======================================
 * // Step 1: Partner-specific adapters convert to unified format
 * UnifiedOrderDTO unifiedA = partnerAAdapter.toUnifiedOrder(partnerAOrder);
 * UnifiedOrderDTO unifiedB = partnerBAdapter.toUnifiedOrder(partnerBOrder);
 *
 * // Step 2: Single mapper converts unified format to entity
 * OrderEvent eventA = orderMapper.toEntity(unifiedA);  // 5 @Mapping annotations (only once!)
 * OrderEvent eventB = orderMapper.toEntity(unifiedB);  // Uses same mapper!
 *
 * Benefits:
 * - Only 5 mapping annotations total (forever!)
 * - Adding Partner C = just create new adapter class (no mapper changes)
 * - Partner logic isolated in separate classes
 * - Easy to test each adapter independently
 * - Follows SOLID principles (Single Responsibility, Open/Closed)
 *
 *
 * USAGE IN SERVICE LAYER:
 * =======================
 *
 * {@code
 * @Service
 * public class OrderIngestionService {
 *     private final PartnerAOrderAdapter partnerAAdapter;
 *     private final PartnerBOrderAdapter partnerBAdapter;
 *     private final OrderMapper orderMapper;
 *     private final OrderEventRepository repository;
 *
 *     public OrderEvent ingestOrderFromPartnerA(PartnerAOrder order) {
 *         // Step 1: Adapter converts partner format to unified format
 *         UnifiedOrderDTO unified = partnerAAdapter.toUnifiedOrder(order);
 *
 *         // Step 2: Mapper converts unified format to entity
 *         OrderEvent event = orderMapper.toEntity(unified);
 *
 *         // Step 3: Service sets additional fields
 *         event.setSequenceNumber(generateSequence("A"));
 *         event.setReceivedTime(LocalDateTime.now());
 *         event.setProcessedTime(LocalDateTime.now());
 *
 *         // Step 4: Persist
 *         return repository.save(event);
 *     }
 *
 *     public OrderEvent ingestOrderFromPartnerB(PartnerBOrder order) {
 *         // Same pattern - just different adapter!
 *         UnifiedOrderDTO unified = partnerBAdapter.toUnifiedOrder(order);
 *         OrderEvent event = orderMapper.toEntity(unified);
 *         event.setSequenceNumber(generateSequence("B"));
 *         event.setReceivedTime(LocalDateTime.now());
 *         event.setProcessedTime(LocalDateTime.now());
 *         return repository.save(event);
 *     }
 * }
 * }
 *
 *
 * OR USING FACTORY PATTERN (Even Better!):
 * =========================================
 *
 * {@code
 * @Service
 * public class OrderIngestionService {
 *     private final PartnerAdapterFactory adapterFactory;
 *     private final OrderMapper orderMapper;
 *     private final OrderEventRepository repository;
 *
 *     public OrderEvent ingestOrder(String partnerId, Object partnerOrder) {
 *         // Step 1: Get the right adapter
 *         PartnerOrderAdapter adapter = adapterFactory.getAdapter(partnerId);
 *
 *         // Step 2: Convert to unified format (type-safe with generics)
 *         UnifiedOrderDTO unified = adapter.toUnifiedOrder(partnerOrder);
 *
 *         // Step 3: Convert to entity
 *         OrderEvent event = orderMapper.toEntity(unified);
 *
 *         // Step 4: Set additional fields
 *         event.setSequenceNumber(generateSequence(partnerId));
 *         event.setReceivedTime(LocalDateTime.now());
 *         event.setProcessedTime(LocalDateTime.now());
 *
 *         // Step 5: Persist
 *         return repository.save(event);
 *     }
 * }
 * }
 *
 *
 * ADDING NEW PARTNERS:
 * ====================
 *
 * To add Partner C, you only need to:
 * 1. Create PartnerCOrder DTO
 * 2. Create PartnerCOrderAdapter implementing PartnerOrderAdapter<PartnerCOrder>
 * 3. That's it! No changes to mapper, service, or any other code.
 *
 * {@code
 * @Component
 * public class PartnerCOrderAdapter implements PartnerOrderAdapter<PartnerCOrder> {
 *     @Override
 *     public UnifiedOrderDTO toUnifiedOrder(PartnerCOrder order) {
 *         // Partner C specific conversion logic
 *         return UnifiedOrderDTO.builder()
 *                 .productCode(order.getProductId())
 *                 .eventTime(convertPartnerCDate(order.getTimestamp()))
 *                 .grossAmount(order.getPrice())
 *                 .discount(order.getDiscountAmount())
 *                 .netAmount(order.getFinalPrice())
 *                 .partnerId("C")
 *                 .build();
 *     }
 *
 *     @Override
 *     public String getPartnerId() {
 *         return "C";
 *     }
 * }
 * }
 *
 * Spring will automatically register it, and PartnerAdapterFactory will find it!
 *
 *
 * SCALABILITY COMPARISON:
 * =======================
 *
 * | Metric                    | Old Approach | New Approach |
 * |---------------------------|--------------|--------------|
 * | Mapping annotations       | 11 × N       | 5 (constant) |
 * | Classes to modify (add C) | 1 (mapper)   | 0            |
 * | New classes (add C)       | 0            | 1 (adapter)  |
 * | Testability               | Medium       | High         |
 * | Code duplication          | High         | None         |
 *
 * Where N = number of partners
 *
 * For 5 partners:
 * - Old: 55 mapping annotations
 * - New: 5 mapping annotations + 5 adapter classes
 */
public class AdapterPatternUsageExample {
    // This class is just documentation - no actual code needed
}

