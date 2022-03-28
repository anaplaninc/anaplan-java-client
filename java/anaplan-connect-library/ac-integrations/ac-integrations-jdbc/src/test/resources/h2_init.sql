drop table if EXISTS TestUserTable;

create memory table TestUserTable (
  colA varchar(255) not null PRIMARY KEY,
  colB BIGINT not null,
  colC varchar(255) not null
);

create memory table TestExportTable (
  colA varchar(255) not null PRIMARY KEY,
  colB varchar(255),
  colC varchar(255)
);

insert into TestUserTable values ('id1', 1, 'colB-Value1');
insert into TestUserTable values ('id2', 2, 'colB-Value2');
insert into TestUserTable values ('id3', 3, 'colB-Value3');
insert into TestUserTable values ('id4', 4, 'colB-Value4');
insert into TestUserTable values ('id5', 5, 'colB-Value5');
insert into TestUserTable values ('id6', 6, 'colB-Value6');
insert into TestUserTable values ('id7', 7, 'colB-Value7');
insert into TestUserTable values ('id8', 8, 'colB-Value8');
insert into TestUserTable values ('C$A&,Z(*yV@lue',123,'W@K!A');

create alias testreverse as $$
  String testReverse(String s) {
      return new StringBuilder(s).reverse().toString();
  }
$$;
