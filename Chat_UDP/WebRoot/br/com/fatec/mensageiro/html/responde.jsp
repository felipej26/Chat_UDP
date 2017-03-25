<html>
  <head>
    <meta charset="UTF-8">
    <title>Chat UDP</title>
    <link rel="stylesheet" href="../../../../../resources/css/estilos.css">
  </head>

  <body>
    <div class="container">
      <div class="usuarios" id="panelUsuarios">
        <section>
	      <h2 id="usuarios">Usuarios</h2>
	     
	      <div class="box-user" id="userMain">
	      	Aguarde ...
		  </div>
		  
		  <div id="divPai">
		  	Aguarde ...
		  </div>
		  
		  <h3 id="select"> </h3>
	    </section>
      </div>
      
      <div class="chat" id="panelMsg">
        Aguarde ...
      </div>
	  
      <div class="formulario">
        <!-- <form action="#" method="post" id="ajax"> -->
        <form>	
          <input type="checkbox" id="chkWhisper" name="whisper">
          <input type="text" id="txtMsg" name="msg" autofocus>
          <input type="submit" id="btnEnviar" name="submit" value="Enviar" disabled="disabled">
        </form>
      </div>
      
      <div>
      	<input type="button" value="Sair" onclick="sair()">
      </div>
    </div>
	
    <script src="../../../../../resources/js/jquery.js"></script>
    <script src="../../../../../resources/js/funcoes.js"></script>
  </body>
</html>