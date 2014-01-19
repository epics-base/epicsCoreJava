package org.epics.pvaccess.client.pvms.util.test;

import org.epics.pvaccess.client.pvms.util.CityHash64;

import junit.framework.TestCase;

public class CityHash64Test extends TestCase{

	/**
	 * @param name
	 */
	public CityHash64Test(String name) {
		super(name);
	}

	private static long[] ZEROS_HASH = {
		0x9ae16a3b2f90404fL,
		0x085f654e398e757cL,
		0xd729d5b7220f2b47L,
		0xe0bc8e2ec18e63a2L,
		0x55e91c7b0a5833bdL,
		0x955507e61e035e91L,
		0xf10bd4e18293896bL,
		0x48ef9aa90e72cd65L,
		0xd7c06285b9de677aL,
		0x0116944d05d4e1a0L,
		0x636d9cb7aeda61c6L,
		0x2cd2ac4717861a5eL,
		0xfbb3b512e22484f0L,
		0xfd5c51fda9f695e6L,
		0x26515f47d70b7cecL,
		0x7c7822834a0290fcL,
		0xfd4bbca2880ba7dcL,
		0x80eff312d5a169c0L,
		0x371481dbd1e25372L,
		0x9c2a905c7580b970L,
		0x3c460e498f75d6ebL,
		0x504173be694b08e5L,
		0x87531d163c226829L,
		0x57c3a1a4b2b76a7fL,
		0x0a3c1163502e08f3L,
		0xd0854fa155a4348aL,
		0xd62ae7eb05a9db91L,
		0x7936a3f114d13712L,
		0xf16d622342b9df5fL,
		0x68b523d84eddd4e4L,
		0x505fd5d635eeedcbL,
		0x23fa4e5a71eb24ffL,
		0x581c6d2fa8b06c65L
	};
	
	public void testZeros()
	{
		byte[] zeros = new byte[ZEROS_HASH.length];
		for (int i = 0; i < ZEROS_HASH.length; i++)
			assertEquals(ZEROS_HASH[i], CityHash64.cityHash64(zeros, 0, i));
	}

	private static long[] HASHES = {
		0x9ae16a3b2f90404fL,
		0x085f654e398e757cL,
		0x354dca3bc03c51d8L,
		0x34e803dc175e241fL,
		0xc6803d385ba50e93L,
		0xb85d8257a350641cL,
		0x3c6c98f92a2c7c87L,
		0xcfabff25b05cbc4fL,
		0xbab32314ab07fa4eL,
		0xf5cb477f28a07fe7L,
		0xcedce60b1fd7b5d3L,
		0x9b73366cb3b6799bL,
		0x00d72829e9ea87e8L,
		0x465271e8b308a98eL,
		0x5e9cd59c40437d62L,
		0x8d3973a9cddcf26fL,
		0x2fb0d75f94362763L,
		0xe5aa5ed150b8e380L,
		0x86072ad7892e0e98L,
		0xf52a57ef93b7c43eL,
		0x813ffac36b97bd63L,
		0xae8a85cc8b2fabe4L,
		0xb20494addc89dca5L,
		0x7cc0c118e72ccf63L,
		0xc5d62bba8bc03888L,
		0x81c5ed69f391ebd8L,
		0x03d38553030c22ceL,
		0x0f190184101b4b05L,
		0xb2280690f03217feL,
		0xab0d5add1a7d81edL,
		0xedbc6aca04b65426L,
		0xca1c6d8816305308L,
		0x40cfef3d008869dcL,
		0x532c06f602b7f406L,
		0x898cc8eec60781d4L,
		0xb12b392a0df2ede2L,
		0xb424325177c06eafL,
		0xc656fb20f8ec26b1L,
		0x8e421c92771ef7d4L,
		0x0c5206ec67c24d23L,
		0x68af4f0da1e7c590L,
		0xfa2be04377495328L,
		0x922d2d54b80419b0L,
		0x1021645f0bc6bb95L,
		0x3360359fc62c1ab8L,
		0x518740c5ca3688d7L,
		0x32814fe50cfa6f10L,
		0xd3685fc55e5c5ea3L,
		0xeda39d75a047d9e7L,
		0x3631fa4950374e70L,
		0xc78a54fccd3809fcL,
		0xb5591eff7cb3e09fL,
		0x0c4652ffefad841cL,
		0x53890442a3d81352L,
		0x950970b9195c4275L,
		0x2da5ace88e1b5c5bL,
		0x2753863cef6fc748L,
		0xbc691ca6ae95eda3L,
		0xc191dfb44a525ad7L,
		0xabbac89cf9804f6aL,
		0x5d5d3bbb94e2455aL,
		0x4ce97b263b4d3db2L,
		0x66ad9ab1f6de0668L,
		0xbedad502410ef96dL,
		0xf7a2aca4d0a3fde1L,
		0xac589c990483dd2eL,
		0xaf6f3ea7de248f72L,
		0x15452a0335f8d3ffL,
		0x87faaf9ffc00b792L,
		0xd735fd9242f41d1dL,
		0xbebcb91fd6057b44L,
		0x0177474a5d1ebbe6L,
		0x9273be0076008b09L,
		0x4ff1e9fb068a6a2bL,
		0xa9bd698beab91622L,
		0x112ef9fd43f6ab0cL,
		0xbd41cfd5fb432bafL,
		0x20959f28fef778b5L,
		0x7dc5c0f6bffa9f3aL,
		0x5d8d82ca24381650L,
		0x4feedc7dada54816L,
		0x993dad25f4602235L,
		0x48ac17a39fa67d56L,
		0xa69f17df1e2b88c4L,
		0x62e9f87e7484d48eL,
		0x86b1f8e0d03141a2L,
		0x4ccf1f0ffc2468c4L,
		0xc0828aa177219532L,
		0xffdce2c7bf331b8fL,
		0x6992c7d1be0fd9caL,
		0xd1715e9954348633L,
		0xcd69d2919e30aa60L,
		0xa5f2c86dc3e7480bL,
		0x536f357a66399901L,
		0xb9da9a2379c19379L,
		0x364ae6782f889be4L,
		0xe3f6cd656b9c26beL,
		0xb1541a33562869eaL,
		0x72bf686a93a755afL,
		0xaf958d2eb9ed0dffL
	};
		// just a test on some generated data
	public void testHashing()
	{
		byte[] data = new byte[HASHES.length];
		for (int i = 0; i < HASHES.length; i++)
			data[i] = (byte)i;
			
		for (int i = 0; i < HASHES.length; i++)
			assertEquals(HASHES[i], CityHash64.cityHash64(data, 0, i));
	}
}
