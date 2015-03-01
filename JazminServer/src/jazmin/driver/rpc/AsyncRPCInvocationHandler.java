package jazmin.driver.rpc;

import java.lang.reflect.Method;
import java.util.List;

import jazmin.server.rpc.RPCClient;
import jazmin.server.rpc.RPCSession;

/**
 * @author yama
 * @date Jun 4, 2014
 */
public class AsyncRPCInvocationHandler extends RPCInvocationHandler {
	private Method asyncCallbackMethod;
	public AsyncRPCInvocationHandler(
			JazminRPCDriver driver,
			RPCClient client,
			List<RPCSession> sessions) {
		super(driver,client,sessions);
		asyncCallbackMethod=AsyncCallback.class.getMethods()[0];
	}
	//
	@SuppressWarnings("unchecked")
	@Override
	protected Object invoke0(
			RPCSession session, 
			Object proxy, 
			Method method,
			Object[] args) throws Throwable {
		//class name end with Async,we need to truncate it.
		String clazzName=method.getDeclaringClass().getSimpleName();
		clazzName=clazzName.substring(0,clazzName.length()-5);//5="Async".length
		String serviceId=clazzName+"."+method.getName();
		Object oo[]=new Object[args.length-1];
		System.arraycopy(args, 0, oo, 0, args.length-1);
		AsyncInvokeMessageCallback callback=new AsyncInvokeMessageCallback();
		callback.callback=(AsyncCallback<Object>)args[args.length-1];
		callback.driver=driver;
		callback.method=method;
		callback.serviceId=serviceId;
		callback.callbackMethod=asyncCallbackMethod;
		callback.beforeCall();
		client.invokeAsync(session, serviceId, oo,callback);
		return null;
	}
}
