package com.stefanini.mav.simulador;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.util.Properties;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.easymock.EasyMock;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.stefanini.mav.mensagem.MensagemNaoEncontradaException;

/**
 * Teste unitário para a classe Losango .
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:mav-init-simulador.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
public class LosangoTest
{
	
	private Losango losango = new Losango();
	
	@Before
	public void setUp() {
		
		losango = new Losango();
	}
	
	@Test
	public void lerArquivo() throws IOException {
		
		CharBuffer b = losango.carregarArquivoMensagens();
		MatcherAssert.assertThat(b.remaining(), Matchers.equalTo(1188));
	}
	
	@Test
	public void carrgarPropriedades() throws IOException {
		
		Properties p = losango.carregarPropriedades();
		
		Properties ep = new Properties();
		ep.setProperty("losango.980008.in", "001160450980008               UILSON       1708940029                              0000000019101011960                                                                                001T             ");
		ep.setProperty("losango.980008.out", "008630460980008P4201170358    UILSON  A00621708940029                                                                                                                 Xx   2508201518081502                                                                                01000000001912010194400101HSSOR00200                                                           0000 0000 0000 0000           000000000000000000000000000000000000000000             000000000000000000000000000000000000000000             000000000000000000000000000000000000000000             000000000000000000000000000000000000000000             000000000000000000000000000000000000000000             000000000000000000000000000000000000000000             000000000000000000000000000000000000000000             000000000000000000000000000000000000000000             000000000000000000000000000000000000000000             000000000000000000000000000000000000000000   T1         2HO");
		
		MatcherAssert.assertThat(ep, Matchers.equalTo(p));
	}
	
	
	@Test
	public void receberMensagem() throws IOException, MensagemNaoEncontradaException, URISyntaxException {
		
		Properties p = losango.carregarPropriedades();
		
		IoSession session = EasyMock.mock(IoSession.class);
		WriteFuture wFuture = EasyMock.mock(WriteFuture.class);
		CloseFuture cFuture = EasyMock.mock(CloseFuture.class);
		
		String expected = p.getProperty("losango.980008.out");
		EasyMock.expect(session.write(expected)).andReturn(wFuture);
		EasyMock.expect(session.close(true)).andReturn(cFuture);
		
		EasyMock.replay(session, wFuture, cFuture);
		losango.messageReceived(session, p.getProperty("losango.980008.in"));
		EasyMock.verify(session, wFuture, cFuture);
	}
	
	@Test
	public void receberMensagemPorSocket() throws IOException, MensagemNaoEncontradaException, URISyntaxException {

		Properties p = losango.carregarPropriedades();
		
		String input = p.getProperty("losango.980008.in");
		String expected = p.getProperty("losango.980008.out");
		
		NioSocketConnector connector = new NioSocketConnector();
		IoHandlerAdapter handler = new IoHandlerAdapter(){
			public void messageReceived(IoSession session, Object message) throws Exception {
				System.out.println("Recebendo " + message);
			};
		};
		
		connector.setHandler(handler);
	    connector.setConnectTimeoutMillis(10*1000);
	    connector.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory() ));
	    ConnectFuture future = connector.connect(new InetSocketAddress(9089)).awaitUninterruptibly();
	    
	    Assert.assertEquals(199, input.length());
	    future.getSession().write(input);
	    future.getSession().getConfig().setUseReadOperation(true);
	    ReadFuture toRead = future.getSession().read();
	    toRead.awaitUninterruptibly(10*1000);
	    String recebida = (String) toRead.getMessage();
	    
	    MatcherAssert.assertThat(expected, Matchers.is(Matchers.equalTo(recebida)));
	}

}
