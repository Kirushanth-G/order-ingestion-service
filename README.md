## Design Considerations

- **Used DECIMAL instead of FLOAT or DOUBLE**  
  Floating-point types can introduce precision errors due to how they are represented in binary.  
  DECIMAL ensures exact precision, which is critical for values like financial data or identifiers.

- **Used constraints to assign a single sequence number per partner**  
  Database constraints (such as UNIQUE or composite keys) were applied to guarantee that each partner is assigned only one sequence number, ensuring data integrity and preventing duplicates.
- **Preventing duplicates**
  * **Solution:** Partners may retry requests, leading to duplicate orders. The sequence_number is generated internally, so standard unique constraints fail. Enforced uniqueness on {PartnerID + ProductID + EventTime}.
  * **Trade-off:** If a partner genuinely sells the same product at the *exact millisecond* (highly unlikely for a single user interaction), it is rejected.

- **Used Adapter Design Pattern**
  Instead of generally ingesting dtos thorugh mapper, which makes the code more modular.
  If a new partnerC joins, creating a PartnerCOrderAdapter is enough and zero changes to the serice logics.

- **Sequence generation per partner**
  To make sure we get the relevant next sequence number for a partner faster, I have implemented in-memory caching with database backup. If the system is running continously we will have quick lookup from in-memory cache(HashMap) or else if the app restarts, it will fetch the last sequence from database.
  Also Lazy loading enabled, only the sequence numbers are obtained from the database only when a order comes.
  * **Cloud Fix:** We can use redis for caching if we deploy it as a distributed system.