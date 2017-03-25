window.onload = function() {
	carregaEventosDivs();
	//window.addEventListener("load", loadMensagens(), false);
};

setInterval(function() {
	loadMensagens();
}, 3000);


var request;

function loadMensagens() {
	try {
		// Construção do objeto XMLHttpRequest segundo o tipo de navegador   
 		if(window.XMLHttpRequest) {
     		request = new XMLHttpRequest();    
 		}
 		else if(window.ActiveXObject) {
     		request = new ActiveXObject("Microsoft.XMLHTTP");   
 		}
 		else {
            // XMLHttpRequest não é suportado pelo navegador
     		alert("Seu navegador não suporta os objetos XMLHTTPRequest...");
     		return;
     	}
     	
		request.addEventListener("readystatechange", stateChange, false);
		request.open('POST', '/Chat_UDP/chat', true);
		request.send();
	}
	catch(exception) {
		alert("Falha na Requisição");
	}
}

function stateChange() {
	if (request.readyState == 4 && request.status == 200) {
		
		var dados = JSON.parse(request.responseText);
		var txt = document.getElementById('panelMensagens');
		
		// 
		if (dados.config != null) {
			var dadosConfig = JSON.parse(dados.config);
			
			//$('userMain').text(
			//alert(document.getElementById("userMain").innerHTML);
			document.getElementById("userMain").innerHTML = 
				dadosConfig.nome + "<br>" + 
				"<i>" + dadosConfig.IP + "</i><br>" + 
				"<span class=\"glyphicon glyphicon-user usuarioOnline\"> </span> (" + dadosConfig.timestamp + ")";
			//);
			txt.innerHTML = "";
		}
		
		// Libera ou bloqueia o botão para enviar Mensagens
		if (dados.libera != null) {
			if (dados.libera === "true") {
				document.getElementById("btnEnviar").className = "btn btn-success btn-lg"; // $('btnEnviar').attr('class', 'btn btn-success btn-sm');
				document.getElementById("divPai").innerHTML = ""; // $('divPai').text("");
			}
			else {
				document.getElementById("btnEnviar").className = "btn btn-warning btn-lg"; //$('btnEnviar').attr('class', 'btn btn-warning btn-sm');
				document.getElementById("divPai").innerHTML = "Carregando ..."; // $('divPai').text("Carregando ...");
			}
		}
		
		// Atualiza Painel de Mensagens
		if (dados.msg != null) {
			txt.innerHTML += dados.msg; // txt.text(txt.text().concat(dados.msg));
		}
		
		// Atualiza Lista de Usuários
		if (dados.users != null) {
			//var user = JSON.parse(dados.users);
			
			//$('divPai').empty();
			var myNode = document.getElementById("divPai");
			var fc = myNode.firstChild;

			while (fc) {
    			myNode.removeChild( fc );
    			fc = myNode.firstChild;
			}
			
			for (var i in dados.users) {
				var user = dados.users[i];
				var icone = "";
				
				if (user.status == "1") {
					icone = "<span class=\"glyphicon glyphicon-user usuarioOnline\"> </span>";
				}
				else {
					icone = "<span class=\"glyphicon glyphicon-user usuarioAusente\"> </span>";
				}
				
				//$('divPai').append(
				document.getElementById("divPai").innerHTML += 	
					"<div class=\"box-user click-me\" id=\"" + user.address + "\">" +
						user.nickname + "<br>" +
						"<i>" + user.address + "</i><br>" +
						icone + "(" + user.timestamp + ")</div>";
				//);
			}
			
			carregaEventosDivs();
		}
    }
}

function sair() {
	var dados = "evento=sair";
	
	alert(dados);
	$.ajax({
		type: 'post',
		url: '/Chat_UDP/chat',
		data: dados,
		success: function() {
			alert("Você saiu da Sala!");
		}
	});
}

$('form').on('submit', function(e) {
	
	e.preventDefault();
	
    var form = $(this);
    //var check = $('chkWhisper').is(':checked');
	var check = document.getElementById("chkWhisper");
	var text = document.getElementById("txtMsg");
    
    if (check.checked == true) {
    	
    	if (count == 1) {
    		var element = document.getElementByClassName('selecionado');
    		var dados = "evento=chat" +
    		"&whisper=" + element[0].id +
    		"&msg=" + text.value;
    		
    		$.ajax({
    			type: 'post',
    			url: '/Chat_UDP/chat',
    			data: dados,
    			success: function() {
    				
    			}
    		});
    	}
    	else {
    		
    		if (count === 0) {
    			
    			alert("Nenhum usuário selecionado");
				//$('txtMsg').text(texto);
				return;
    		}
			else {
				
				alert("Somente um usuário pode ser selecionado");
				//$('txtMsg').text(texto);
				return;
			}
		}
    }
    else {
    	if (count === 1) {
    		var element = document.getElementByClassName('selecionado');
    		var dados = "evento=chat" +
    		"&target=" + element[0].id +
    		"&msg=" + text.value;
    		
    		$.ajax({
    			type: 'post',
    			url: '/Chat_UDP/chat',
    			data: dados,
    			success: function() {
    				
    			}
    		});
    		
    	}
    	else {
    		
			var dados = "evento=chat" +
    		"&users=all" +
    		"&msg=" + text.value;
    		
    		$.ajax({
    			type: 'post',
    			url: '/Chat_UDP/chat',
    			data: dados,
    			success: function() {
    				
    			}
    		});
    	}
    }
    
	text.value = "";
});

var count = 0;

function clickMe() {
	if (this.className == 'click-me selecionado') {
		this.className = 'click-me desselecionado';
		count -= 1;
	}
	else {
		if (count === 1) {
			alert("Somente um usuário pode ser selecionado");
		}
		else {
			this.className = 'click-me selecionado';
			count += 1;
		}
	}
	
	//verificaDivs();
}

/*
function verificaDivs() {
	if (count === 1) {
		$('select').text("1 usuário selecionado");
	}
	else {
		$('select').text(count + " usuários selecionados");
	}
}
*/

function carregaEventosDivs() {
	var divs = document.getElementsByClassName('click-me');
	
	for (var i = 0; i < divs.length; i++) {
		divs[i].onclick = clickMe;
	}
	
	//verificaDivs();
}
