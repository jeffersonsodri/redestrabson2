# redestrabson2
### 1. Descrição do Trabalho
 
Neste trabalho, você implementará uma versão simplificada do protocolo TCP para transmissao confiável de dados.  A sua implementaçao consistirá em uma aplicação cliente e outra servidora.   A  aplicação  cliente  transmitira  dados  de  um  arquivo  encapsulados  em  segmentos TCP, mas transmitidos em datagramas UDP. Essa aplicação deve implementar o algoritmo de
janela deslizante do TCP para transmissao confiável, levando em consideracao o valor anunciado na janela do receptor. Os pacotes transmitidos e n ̃ao confirmados devem ser mantidos em um buffer na memoria, ou seja, eles nao podem ser lidos diretamente do disco para retransmissao. A aplicação servidora ira receber os datagramas UDP, desencapsular os segmentos TCP, enviar segmentos ACK de confirmação e armazenar os dados recebidos em disco somente quando eles estiverem em ordem, ou seja, os segmentos recebidos fora de ordem devem ser colocados em um buffer de recepção. As aplicações cliente e servidora devem ser executadas com as linhas de comando abaixo: 


	$ tcp_client fn sip sport wnd rto mss dupack lp
	fn:     nome do arquivo a ser enviado
	sip:    endereço IP do servidor
	sport:  porta UDP do servidor
	wnd:    tamanho da janela do transmissor e receptor em bytes
	rto:    valor inicial de timeout para retransmiss~ao de um segmento em milisegundos
	mss:    TCP Maximum Segment Size
	dupack: deve ser um para usar retransmiss~ao via ACKs duplicados e zero caso contrário
	lp:     probabilidade de um datagrama UDP ser descartado
	$ tcp_server fn sport wnd lp
	fn:    nome do arquivo a ser recebido e gravado em disco
	sport: porta UDP que o servidor deve escutar
	wnd:   tamanho da janela do transmissor e receptor em bytes
	lp:    probabilidade de um datagrama UDP ser descartado 

### 1.1.    Estabelecimento de Sessao 

Voce deve implementar a fase de estabelecimento de sessao do TCP (three-way handshake) antes de comecar a transmitir os dados.  Nesta fase, os valores da janela do receptor e de números de sequencia iniciais devem ser negociados. Lembre-se que o numero de sequencia inicial de uma sessao TCP e um valor aleatorio de 32 bits. 


### 1.2.    Confirmacoes Cumulativas

Os segmentos de confirmação (ACKs) devem ser cumulativos, como no TCP.


### 1.3.    Retransmissões 

Voce deve implementar duas formas de retransmissao de segmentos perdidos:  ACKs dupli-cados e timeout.  Ao receber tres ACKs com o mesmo valor, voce assume que o segmento com numero de sequencia no campo ACK do segmento TCP foi perdido e precisa ser retransmitido. Para retransmissoes com timeout, voce deve calcular o valor do timeout em funcao dos tempos de envio dos segmentos e os tempos de recebimento de suas confirmacoes.  Utilize o algoritmo padrao do TCP para atualizacao do valor do timeout.  Para os segmentos SYN e SYN+ACK, os valores de timeout a serem utilizados sao os fornecidos na linha de comando.  A retransmissao por ACKs duplicados pode ser abilitada ou desabilitada pelo parametro dupack da linha de comando. A retransmissao por timeout e sempre ativa, mas um segmento pode ser retransmitido antes por ACKs duplicados.

### 1.4.    Simulando Perdas de Pacote

Voce deve implementar uma funcao de envio de datagramas UDP que recebe como parametro um valor de probabilidade de descarte de pacotes (esse valor e passado na linha de comando). Para cada datagrama a ser transmitido, voce deve gerar um numero aleatorio entre 0 e 1. Se o  valor  aleatorio  gerado  for  maior  do  que  a  probabilidade  de  descarte, o  datagrama  deve  ser transmitido. Caso contrario, o datagrama deve ser descartado.
