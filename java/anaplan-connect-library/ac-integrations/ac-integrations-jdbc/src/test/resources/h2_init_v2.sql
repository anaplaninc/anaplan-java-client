drop table if EXISTS TestJnJTable;

CREATE TABLE TestJnJTable (
  `KEY_CONCAT` nvarchar(200) DEFAULT NULL,
  `FiscalPeriod_Concat` nvarchar(200) DEFAULT NULL,
  `RYEAR` nvarchar(200) DEFAULT NULL,
  `POPER` nvarchar(200) DEFAULT NULL,
  `RVERS` nvarchar(200) DEFAULT NULL,
  `RBUKRS` nvarchar(200) DEFAULT NULL,
  `SEGMENT` nvarchar(200) DEFAULT NULL,
  `RACCT` nvarchar(200) DEFAULT NULL,
  `PRCTR` nvarchar(200) DEFAULT NULL,
  `PRODUCT` nvarchar(200) DEFAULT NULL,
  `RTCUR` nvarchar(200) DEFAULT NULL,
  `TSLC` DECIMAL(6,3) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create alias testreverse as $$
  String testReverse(String s) {
      return new StringBuilder(s).reverse().toString();
  }
$$;
