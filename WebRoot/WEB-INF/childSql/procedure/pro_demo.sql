DELIMITER $$
drop PROCEDURE if EXISTS pro_abc$$
CREATE PROCEDURE `pro_abc`(
OUT totalRow INT,  -- 返回总行数
OUT totalPage INT, -- 返回总页数
IN pageIndex int,
IN pageSize INT,
in startTime varchar(10), --剩余参数的名称必须和前端传过来的参数名称一致
IN endTime varchar(10),
IN accountId INT,
IN isRcw INT
)
BEGIN
/*分页算法：(传入limit条件的变量)*/
SET @startRow = pageSize*(pageIndex-1);
set @pageSize = pageSize;

/*使用字符串拼接方法执行sql语句，其中 SQL_CALC_FOUND_ROWS 变量是记录符合查询条件的总数，该变量会忽略limit条件来计数*/
SET @sqlstr = '
select SQL_CALC_FOUND_ROWS his.*,pay.* ,
sum(case when pay.type = 0 then pay.payMoney else null  end) as addMoney ,
sum(case when pay.type != 0 then pay.payMoney else null  end) as subMoney ,
billType.name as billTypeName,staff.*,accounts.* 
from bb_billhistory as his 
left join cw_pay_type as pay on his.billId = pay.billId and his.billTypeId = pay.billTypeId 
left join sys_billtype as billType on his.billTypeId = billType.id 
left join b_staff as staff on staff.id = his.staffId 
left join b_accounts as accounts on accounts.id = pay.accountId 
where 1=1';
-- where 1=1 and his.recodeDate BETWEEN ''2014-12-01'' and ''2015-04-20'' and accounts.pids like ''%{37}%'' 

/*查询条件：*/

if (IFNULL(startTime,'') <> '') THEN
set @sqlstr = CONCAT(@sqlstr,' and his.recodeDate BETWEEN ''',startTime,''' and ''',endTime,'''');
END IF;

if (IFNULL(accountId,'') <> '') THEN
set @sqlstr = CONCAT(@sqlstr,' and accounts.pids like ''%{',accountId,'}%''');
end if;

if (IFNULL(isRcw,'') <> '') THEN
set  @sqlstr = CONCAT(@sqlstr,' and his.isRcw =',isRcw);
end IF;

SET @sqlstr = CONCAT(@sqlstr,' group by pay.accountId,his.billId,pay.billTypeId LIMIT ',@startRow,',',@pageSize);

-- select @sqlstr ;
PREPARE sqlstr from @sqlstr; -- 把sql语句放入预处理变量
EXECUTE sqlstr;							 -- 执行sql语句
DEALLOCATE PREPARE sqlstr;   -- 关掉预处理命令

set totalRow = FOUND_ROWS(); -- 返回查询总数，该数目忽略limit条件，前提是sql语句必须使用 SQL_CALC_FOUND_ROWS 变量来计数

/*计算结果集总共可以分成多少页：*/
if(totalRow <=pageSize)THEN
	SET totalPage = 1;
else IF(totalRow%pageSize>0) THEN
	set totalPage = totalRow/pageSize+1;
ELSE
	SET totalPage = totalRow/pageSize;
	end if;
END if;
-- create TEMPORARY TABLE tmp_table select * from xxx

END$$
DELIMITER ;