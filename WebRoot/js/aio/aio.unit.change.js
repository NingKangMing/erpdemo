//请使用  shift+cltr+/ 查看方法分布（切记别乱写）
//单位关系   换算


//单位对应的数量     旧单位(oldSelectUnitId)换算成新单位(selectUnitId)
function unitLitterAmount(amount,oldSelectUnitId,selectUnitId,unitRelation1,unitRelation2,unitRelation3){
	if(!$.isNumeric(amount)||amount==0||!$.isNumeric(oldSelectUnitId)||oldSelectUnitId==0){
		return 0;
	}
	var litterAmount=0;
	if(oldSelectUnitId==1&&$.isNumeric(unitRelation1)&&unitRelation1!=0){
		litterAmount=amount;
	}else if(oldSelectUnitId==2&&$.isNumeric(unitRelation2)&&unitRelation2!=0){
		litterAmount=amount/unitRelation2;
	}else if(oldSelectUnitId==3&&$.isNumeric(unitRelation3)&&unitRelation3!=0){
		litterAmount=amount/unitRelation3;
	}
	
	if(selectUnitId==1&&$.isNumeric(unitRelation1)&&unitRelation1!=0){
		litterAmount=amount;
	}else if(selectUnitId==2&&$.isNumeric(unitRelation2)&&unitRelation2!=0){
		litterAmount=amount*unitRelation2;
	}else if(selectUnitId==3&&$.isNumeric(unitRelation3)&&unitRelation3!=0){
		litterAmount=amount*unitRelation3;
	}
	return litterAmount;
}




//单位对应的价格   旧单位(oldSelectUnitId)换算成新单位(selectUnitId)
function unitLitterPrice(price,oldSelectUnitId,selectUnitId,unitRelation1,unitRelation2,unitRelation3){
	if(!$.isNumeric(price)||price==0||!$.isNumeric(oldSelectUnitId)||oldSelectUnitId==0){
		return 0;
	}
	var litterPrice=0;
	if(oldSelectUnitId==1&&$.isNumeric(unitRelation1)&&unitRelation1!=0){
		litterPrice=price;
	}else if(oldSelectUnitId==2&&$.isNumeric(unitRelation2)&&unitRelation2!=0){
		litterPrice=price/unitRelation2;
	}else if(oldSelectUnitId==3&&$.isNumeric(unitRelation3)&&unitRelation3!=0){
		litterPrice=price/unitRelation3;
	}
	
	
	if(selectUnitId==1&&$.isNumeric(unitRelation1)&&unitRelation1!=0){
		litterPrice=price;
	}else if(selectUnitId==2&&$.isNumeric(unitRelation2)&&unitRelation2!=0){
		litterPrice=litterPrice*unitRelation2;
	}else if(selectUnitId==3&&$.isNumeric(unitRelation3)&&unitRelation3!=0){
		litterPrice=litterPrice*unitRelation3;
	}
	return litterPrice;
}



function replaceBaseType(type){
	var $parent = getCurrentPanel();
	$parent.find("#baseType").val(type);
}

