package com.generallycloud.test.nio.balance;

import com.generallycloud.nio.balance.BalanceContext;
import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;
import com.generallycloud.test.nio.common.ReadFutureFactory;

public class TestBalanceBroadcast {

	public static void main(String[] args) throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {

				ProtobaseReadFuture f = (ProtobaseReadFuture) future;
				
				if (BalanceContext.BALANCE_CHANNEL_LOST.equals(f.getFutureName())) {
					System.out.println("客户端已下线：" + f.getReadText());
				} else {
					System.out.println("~~~~~~收到报文：" + future.toString());
					String res = "(***" + f.getReadText() + "***)";
					System.out.println("~~~~~~处理报文：" + res);
					f.write(res);
					session.flush(future);
				}
			}
		};

		ServerConfiguration configuration = new ServerConfiguration(8800);

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.getContext().setProtocolFactory(new ProtobaseProtocolFactory());
		
		connector.connect();

		SocketSession session = connector.getSession();

		for (;;) {

			ProtobaseReadFuture future = ReadFutureFactory.create(session, "broadcast");

			future.write("broadcast msg");

			session.flush(future);

			ThreadUtil.sleep(2000);
		}
	}

}