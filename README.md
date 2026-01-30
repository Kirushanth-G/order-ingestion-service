## Design Considerations

- **Used `DECIMAL` instead of `FLOAT` or `DOUBLE`**  
  Floating-point types can introduce precision errors due to how they are represented in binary.  
  `DECIMAL` ensures exact precision, which is critical for values like financial data or identifiers.

- **Used constraints to assign a single sequence number per partner**  
  Database constraints (such as `UNIQUE` or composite keys) were applied to guarantee that each partner is assigned only one sequence number, ensuring data integrity and preventing duplicates.
