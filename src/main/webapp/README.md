CTS Project - Final Structure

This archive contains the agreed project structure only.
- JDBC architecture (no Hibernate / no Maven pom.xml)
- Controllers, DAO/DAOImpl, Service/ServiceImpl, Validator/ValidatorImpl
- Grouped packages for related classes
- Separate inward and outward areas
- Reusable ZK components for sidebar/header/footer/cheque image viewer
- ZUL and CSS placeholders matching the screen structure
- WEB-INF/lib is intentionally empty; add the required ZK and JDBC JARs locally.
- db.properties is a placeholder and is ignored by Git.

Team members should implement the Java classes and ZUL/CSS contents.
