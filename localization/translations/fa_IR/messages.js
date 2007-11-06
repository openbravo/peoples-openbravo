/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
//Valores por defecto
var LNG_POR_DEFECTO = "en_US";
var TIPO_POR_DEFECTO = 0;

function tipoMensaje(intMensaje, intTipo)
{
	this.Mensaje = intMensaje;
	this.Tipo = intTipo;
}

function textoMensajes(intIdioma, intMensaje, strTexto, strPorDefecto)
{
	this.Idioma = intIdioma;
	this.Mensaje = intMensaje;
	this.Texto = strTexto;
	this.PorDefecto = strPorDefecto;
}


//TIPOS
var arrTipos = new Array(
new tipoMensaje(0,0),
new tipoMensaje(1,0),
new tipoMensaje(2,1),
new tipoMensaje(3,0),
new tipoMensaje(4,0),
new tipoMensaje(5,0),
new tipoMensaje(6,0),
new tipoMensaje(7,0),
new tipoMensaje(8,0),
new tipoMensaje(9,0),
new tipoMensaje(10,1),
new tipoMensaje(11,0),
new tipoMensaje(12,0),
new tipoMensaje(13,0),
new tipoMensaje(14,0),
new tipoMensaje(15,0),
new tipoMensaje(16,1),
new tipoMensaje(17,0),
new tipoMensaje(18,0)
);


//MENSAJES
var arrMensajes = new Array(
//ESPAÑOL
new textoMensajes("es_ES", 0, "Se ha procesado correctamente la información", null),
new textoMensajes("es_ES", 1, "No ha introducido todos los datos necesarios para continuar", null),
new textoMensajes("es_ES", 2, "Se va a proceder a eliminar.\n¿Esta seguro de que desea continuar?", null),
new textoMensajes("es_ES", 3, "Esta opción no está habilitada", null),
new textoMensajes("es_ES", 4, "El texto introducido no es numérico", null),
new textoMensajes("es_ES", 5, "La fecha introducida no es correcta o no está escrita en el formato preciso", null),
new textoMensajes("es_ES", 6, "No se ha podido procesar la información", null),
new textoMensajes("es_ES", 7, "Este campo es obligatorio y no pueden estar en blanco", null),
new textoMensajes("es_ES", 8, "Ha superado el máximo de respuestas permitido", null),
new textoMensajes("es_ES", 9, "El valor introducido se encuentra fuera del rango permitido", null),
new textoMensajes("es_ES", 10, "Se han producido cambios en el registro actual.\n¿Desea continuar y perder los cambios?", null),
new textoMensajes("es_ES", 11, "Este campo ya está lleno y no admite más caracteres", null),
new textoMensajes("es_ES", 12, "La hora introducida no es correcta o no está escrita en el formato correcto", null),
new textoMensajes("es_ES", 13, "Existe más de un registro seleccionado", null),
new textoMensajes("es_ES", 14, "No se puede realizar la transferencia a caja y banco simultaneamente", null),
new textoMensajes("es_ES", 15, "No se puede realizar la transferencia desde caja y banco simultaneamente", null),
new textoMensajes("es_ES", 16, "Se va a proceder a modificar el registro.\n¿Esta seguro de que desea continuar?", null),
new textoMensajes("es_ES", 17, "Se ha superado el total", null),
new textoMensajes("es_ES", 18, "No se ha asignado el total", null),
//INGLES
new textoMensajes("en_US", 0, "The data has been correctly processed", null),
new textoMensajes("en_US", 1, "You have not filled in all needed fields", null),
new textoMensajes("en_US", 2, "The record will be deleted.\nAre you sure you want to continue?", null),
new textoMensajes("en_US", 3, "This option is disabled", null),
new textoMensajes("en_US", 4, "The text typed is not a number", null),
new textoMensajes("en_US", 5, "The date entered is incorrect or is not in the correct format", null),
new textoMensajes("en_US", 6, "The data could not be processed", null),
new textoMensajes("en_US", 7, "This field is needed and it cannot be blank", null),
new textoMensajes("en_US", 8, "The answers are more than what are allowed", null),
new textoMensajes("en_US", 9, "The value is out of a valid range", null),
new textoMensajes("en_US", 10, "There are changes in the current record.\nDo you want to continue? (You will lose your changes)", null),
new textoMensajes("en_US", 11, "This field is full and cannot receive more characters", null),
new textoMensajes("en_US", 12, "The time entered is incorrect or is not in the correct format", null),
new textoMensajes("en_US", 13, "There're more than one record selected", null),
new textoMensajes("en_US", 14, "Transfer from cash and bank at once is not allowed", null),
new textoMensajes("en_US", 15, "Transfer to cash and bank at once is not allowed", null),
new textoMensajes("en_US", 16, "You are modifying this record.\nAre you sure you want to continue?", null),
new textoMensajes("en_US", 17, "The total is over the limit", null),
new textoMensajes("en_US", 18, "The total hasn't been assigned", null),

//Persian
new textoMensajes("fa_IR", 0, " داده با موفقیت پردازش شد ", null),
new textoMensajes("fa_IR", 1, " شما فیلدهای لازم را پر نکردید ", null),
new textoMensajes("fa_IR", 2, " رکورد حذف خواهد شد \n آیا مطمئن هستید ؟ ", null),
new textoMensajes("fa_IR", 3, " این انتخاب غیرفعال شد ", null),
new textoMensajes("fa_IR", 4, " فقط عدد وارد کنید ", null),
new textoMensajes("fa_IR", 5, " تاریخ وارد شده فرمت اشتباه دارد ", null),
new textoMensajes("fa_IR", 6, " داده ها قابل پردازش نیست ", null),
new textoMensajes("fa_IR", 7, " این فیلد ضروری است و نمی تواند خالی باشد ", null),
new textoMensajes("fa_IR", 8, " جواب ها بیش از مقدار اجازه داده شده است ", null),
new textoMensajes("fa_IR", 9, " مقدار خارج از بازه معتبر است ", null),
new textoMensajes("fa_IR", 10, " تغییراتی در رکورد جاری ایجاد شده \n آیا می خواهید ادامه دهید ؟ \n تغییرات از دست خواهد رفت ", null),
new textoMensajes("fa_IR", 11, " این فیلد پر شده و کاراکتر اضافه نمی توان در آن وارد کرد ", null),
new textoMensajes("fa_IR", 12, "زمان وارد شده فرمت اشتباه دارد", null),
new textoMensajes("fa_IR", 13, " بیش از یک رکورد انتخاب شده ", null),
new textoMensajes("fa_IR", 14, " انتقال نقدی و بانکی در یک زمان ممکن نیست ", null),
new textoMensajes("fa_IR", 15, "انتقال نقدی و بانکی در یک زمان ممکن نیست ", null),
new textoMensajes("fa_IR", 16, " شما در حال ویرایش رکورد هستید \n آیا می خواهید ادامه دهید ؟ ", null),
new textoMensajes("fa_IR", 17, " مجموع بیش از حد مجاز است ", null),
new textoMensajes("fa_IR", 18, " مجموع واگذار نشده ", null),


//pt_PT
new textoMensajes("pt_PT", 0, "Os dados foram processados correctamente", null),
new textoMensajes("pt_PT", 1, "Não preencheu todos os campos necessários", null),
new textoMensajes("pt_PT", 2, "O registo será eliminado.\nTem a certeza que deseja continuar?", null),
new textoMensajes("pt_PT", 3, "Esta opção está desabilitada", null),
new textoMensajes("pt_PT", 4, "O texto introduzido não é um número", null),
new textoMensajes("pt_PT", 5, "A data introduzida está incorrecta ou não está no formato correcto", null),
new textoMensajes("pt_PT", 6, "Os dados não puderam ser processados", null),
new textoMensajes("pt_PT", 7, "Este campo é necessário e não pode estar vazio", null),
new textoMensajes("pt_PT", 8, "As respostas são mais do que as permitidas", null),
new textoMensajes("pt_PT", 9, "O valor está fora de um intervalo válido", null),
new textoMensajes("pt_PT", 10, "Foram efectuadas alteração ao registo corrente.\nDeseja continuar? (Irá perder todas as alterações)", null),
new textoMensajes("pt_PT", 11, "O campo está completo e não pode aceitar mais caracteres", null),
new textoMensajes("pt_PT", 12, "A hora introduzida está incorrecta ou não está no formato correcto", null),
new textoMensajes("pt_PT", 13, "Está seleccionado mais do que um registo", null),
new textoMensajes("pt_PT", 14, "Não é permitida a transferência de dinheiro e banco ao mesmo tempo", null),
new textoMensajes("pt_PT", 15, "Não é permitida a transferência para dinheiro e banco ao mesmo tempo", null),
new textoMensajes("pt_PT", 16, "Vai modificar este registo.\nTem a certeza que deseja continuar?", null),
new textoMensajes("pt_PT", 17, "O total está para além do limite", null),
new textoMensajes("pt_PT", 18, "O total não foi assignado", null)
);

function obtenerMensaje(index, idioma)
{
	if (idioma==null)
		idioma = LNG_POR_DEFECTO;
  var total = arrMensajes.length;
	for (var i=0;i<total;i++)
	{
		if (arrMensajes[i].Idioma == idioma)
			if (arrMensajes[i].Mensaje == index)
				return (arrMensajes[i].Texto);
	}
	return null;
}

function textoPorDefecto(index, idioma)
{
	if (idioma==null) idioma = LNG_POR_DEFECTO;
  var total = arrMensajes.length;
	for (var i=0;i<total;i++) {
		if (arrMensajes[i].Idioma == idioma)
			if (arrMensajes[i].Mensaje == index)
				return (arrMensajes[i].PorDefecto);
	}
	return null;
}

function obtenerTipo(index)
{
  var total = arrTipos.length;
	for (var i=0;i<total;i++)
	{
		if (arrTipos[i].Mensaje == index)
			return (arrTipos[i].Tipo);
	}
	return null;
}

/*	Los tipos de mensajes son:
		0.- Alert -> muestra una ventana de mensaje normal con un botón aceptar
		1.- Confirm -> muestra una ventana de confirmación que tiene 2 botones (OK y CANCEL)
		2.- Prompt -> muestra una ventana de petición de un parámetro con 2 botones (OK y CANCEL)
*/
function presentarMensaje(strTexto, intTipo, strValorDefecto)
{
	switch (intTipo)
	{
	case 1:return confirm(strTexto);
			 break;
	case 2:return prompt(strTexto, strValorDefecto);
			 break;
	default: alert(strTexto);
	}
	return true;
}

function mensaje(index, idioma)
{
	var strMensaje = obtenerMensaje(index, idioma);
    if (strMensaje == null)  strMensaje = obtenerMensaje(index,"en_US");
	if (strMensaje == null) return false;
	var strPorDefecto = textoPorDefecto(index, idioma);
	if (strPorDefecto == null)  textoPorDefecto(index, LNG_POR_DEFECTO);
	var tipo = obtenerTipo(index, idioma);
	if (tipo==null)
		tipo=TIPO_POR_DEFECTO;
	return presentarMensaje(strMensaje, tipo, strPorDefecto);
}
