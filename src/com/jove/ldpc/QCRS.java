package com.jove.ldpc;

import java.io.PrintWriter;

import com.jove.ldpc.STask.build;

public class QCRS {
	
	public int M;
	public int N;
	public int bl;
	Degree deg;
	int[][] mask;
	PegMask aM;
	BigGirth aGirth;
	
	poly gen;
	int[][] rs;
	int[][] H;
	Shift phyInv;
	STask tA;
	STask tET;
	STask tB;
	STask tT;
	STask tINV;
	STask tCHECK;

	Shift[] info;
	Shift[] result;
	Shift[] memInt;
	PrintWriter OUT;
	public void dumpTask( PrintWriter out )
	{
		out.println("#tA");
		tA.dumpTask(out);
		out.println("#tET");
		tET.dumpTask(out);
		out.println("#tB");
		tB.dumpTask(out);
		out.println("#tT");
		tT.dumpTask(out);
		out.println("#tINV");
		tINV.dumpTask(out);
		out.println("#tCHECK");
		tCHECK.dumpTask(out);
	}
	void setWriter( PrintWriter out )
	{
		OUT = out;
	}
	boolean check()
	{
		int i,j;
		for(i=0;i<M-1;i++)
		{
			if(H[i][N-M+i+1]!=0)
			{
				System.out.println("Fail on 0");
				return false;
			}
			for( j=N-M+i+2;j<N;j++)
				if(H[i][j]!=bl)
				{
					System.out.println("Fail on 1");
					return false;
				}
			}
		if(calcPhyInv(0)==false)
		{
			System.out.println("Fail on 2");
			return false;
		}
		return true;			
	}
	void genMask( )
	{
		aM = new PegMask(deg);
		aM.init(M,N);
		int l = aM.fillMask();
		mask = aM.mask;
		swap(mask,0,M);
	}
	void genMask( int girth)
	{
		aGirth = new BigGirth( M, N, deg, girth);;
		mask = aGirth.getMask();
	}
	void genRS()
	{
		rs = gen.QCRSTab();
	}
	void genH(int offR, int offC)
	{
		H = new int[M][];
		int i,j;
		for( i=0;i<M;i++ )
		{
			H[i] = new int[N];
			assert( mask[i][N-M+i+1]!=0 );
			int sh = rs[(i+offR)%(gen.P)][(N-M+i+1+offC)%(gen.P-1)];
			for( j=0;j<N;j++ )
			{
				if(mask[i][j]==1)
					H[i][j]=(rs[(i+offR)%(gen.P)][(j+offC)%(gen.P-1)]-sh+3*bl)%bl;
				else
					H[i][j]=bl;
			}
		}
	}
	
	int[][] getCol(int b, int e)
	{
		int[][] ret = new int[e-b+1][];
		int i,j;
		for( i=b;i<=e;i++ )
		{
			ret[i-b] = new int[M];
			for( j=0;j<M;j++ )
				ret[i-b][j] = H[j][i];
		}
		return ret;
	}
	
	int[][] getT()
	{
		int[][] ret = new int[M-1][];
		int i,j;
		for( i=N-M+1;i<N;i++ )
		{
			ret[i-N+M-1] = new int[M-1];
			for( j=0;j<M-1;j++ )
				ret[i-N+M-1][j] = H[j][i];
		}
		return ret;
	}
	
	int[][] getET()
	{
		int[][] ret = new int[M-1][];
		int i,j;
		for( i=N-M+1;i<N;i++ )
		{
			ret[i-N+M-1] = new int[M];
			for( j=0;j<M;j++ )
				ret[i-N+M-1][j] = H[j][i];
		}
		return ret;
		//return getCol(N-M+1,N-1);
	}
	int[][] getEB()
	{
		return getCol(N-M,N-M);
	}
	int[][] getB()
	{
		int[][] ret = new int[1][];
		int j;
		ret[0] = new int[M-1];
		for( j=0;j<M-1;j++ )
			ret[0][j] = H[j][N-M];
		return ret;
	}
	
	int[][] getA()
	{
		int[][] ret = new int[M][N-M];
		int i,j;
		for( i=0;i<M;i++ )
		{
			for( j=0;j<N-M;j++ )
			ret[i][j] = H[i][j];
		}
		return ret;
	}
	
	void swap(int[][] T,int a,int b)
	{
		int i,j;
		for( i=0;i<M;i++ )
		{
			j = T[i][a];
			T[i][a]=T[i][b];
			T[i][b]=j;
		}
	}
	
	boolean calcPhyInv(int print)
	{
		int[][] T = getET();
		int[][] B = getEB();
		STask Ttask = new STask( T, M, M-1, bl, build.guass );
		Shift[][] Bsh = Shift.alloc(B);
		int i;
		if( (print&1)==1 )
		{
			System.out.println("Befor ET-1B+D:");
			for(i=0;i<M;i++)
			{
				Bsh[0][i].print();
				System.out.println();
			}
		}
		Ttask.doit(Bsh[0], Bsh[0]);
		if( (print&1)==1 )
		{
			Ttask.printfTask();
			System.out.println("After ET-1B+D:");
			for(i=0;i<M;i++)
			{
				Shift.reverse(Bsh[0][i]).print();
				System.out.println();
			}
		}
		Shift phy = Bsh[0][M-1];
		
		int polyGP = poly.GP;
		poly.init(0x3);
		poly pphy = new poly();
		pphy.int2Poly(phy.buf, bl);
		poly pphyInv = poly.pinv(pphy,bl);
		poly.init(polyGP);
		if( pphyInv==null )
			return false;
		else
		{
			Shift tphyInv = new Shift();
			pphyInv.poly2Int(tphyInv.buf);
			phyInv = Shift.reverse(tphyInv);
			if( (print&1)==1)
				phyInv.printH2L();
			return true;
		}
	}
	public static int getPolybyN( int n )
	{
		int ret = 0x25;
		if( n>=30 )
			ret = 0x43;
		if(n>=62)
			ret = 0x83;
		if(n>=126)
			ret = 0x11d;
		if(n>=254)
			ret = 0x203;
		return ret;
	}
	public static int getPolybyBL( int n )
	{
		int ret = 0x13;
		if( n>31 )
			ret = 0x25;
		if(n>63)
			ret = 0x43;
		if(n>127)
			ret = 0x83;
		if(n>255)
			ret = 0x11d;
		if(n>511)
			ret = 0x203;
		return ret;
	}
	QCRS( Degree deg,int M, int N, int bl, int polynomail )
	{
		this.deg =deg;
		this.M = M;
		this.N = N;
		this.bl = bl;
		Shift.length = bl;
		Shift.init();
		poly.init( polynomail );
		gen = new poly();
		genMask(80);
		genRS();
	}
	public QCRS( int[][] h)
	{
		M = h.length;
		N = h[0].length;
		bl = h[0][N-1];
		H = new int[M][];
		Shift.length = bl;
		Shift.init();
		int i,j;
		for(i=0;i<M;i++)
		{
			H[i] = new int[N];
			for(j=0;j<N;j++)
			{
				H[i][j]=h[i][j];
			}
		}
		if(check()==false)
			System.out.printf("H Check Fail %d %d %d!\n",M,N,bl);
		encoderInit();
	}
	public void encoderInit() 
	{
		tA = new STask( getA(), M, N-M, bl, build.matrix );
		tET = new STask( getET(), M, M-1, bl, build.guass );
		tB = new STask( getB(), 1, M-1, bl, build.matrixT );
		Shift[][] invArray = new Shift[1][];
		invArray[0] = new Shift[1];
		invArray[0][0] = phyInv;
		tINV = new STask( invArray, 1, 1);
		tT  = new STask( getT(), M-1, M-1, bl, build.guass );
		tCHECK = new STask( H, M, N, bl, build.matrix );
		result = new Shift[M];
		int i;
		for( i=0;i<M;i++ )
			result[i] = new Shift();
		memInt = new Shift[M];
		for( i=0;i<M;i++ )
			memInt[i] = new Shift();
		info = new Shift[N-M];
		for( i=0;i<N-M;i++ )
			info[i] = new Shift();
	}
	public void reportTask()
	{
		System.out.printf("ET: \n");
		tET.printfTask();
		System.out.printf("INV: \n");
		tINV.printfTask();
		System.out.printf("T: \n");
		tT.printfTask();
		System.out.printf("B: \n");
		tB.printfTask();
		System.out.printf("A: \n");
		tA.printfTask();
		System.out.printf("CHECK: \n");
		tCHECK.printfTask();
		
	}
	void encShift( int print )
	{
		Shift.clean(result);
		if( ((print>>0)&1) ==1 )
			Shift.printArray(info, "Info :", 3);
		tA.doit( info, result );
		if( ((print>>1)&1) ==1 )
			Shift.printArray(result, "[As Cs]:", 3);
		Shift.cpy(result, memInt);
		tET.doit(memInt, memInt);
		if( ((print>>2)&1) ==1 )
			Shift.printArray(memInt, "[(ET-1)As+Cs] :", 3);
		result[M-1].clean();
		tINV.doit(memInt, result,M-1,M-1);
		if( ((print>>3)&1) ==1 )
		{
			phyInv.print();
			Shift.printArray(result, "(phy-1)[(ET-1)As+Cs] :", 3);
		}
		tB.doit(result, result,M-1,0);
		if( ((print>>4)&1) ==1 )
			Shift.printArray(result, "(phy-1)[(ET-1)As+Cs]B :", 3);
		tT.doit(result, result);
		if( ((print>>5)&1) ==1 )
			Shift.printArray(result, "(T-1)(phy-1)[(ET-1)As+Cs]B :", 3);
	}
	public Shift[] enc()
	{
		int i;
		encShift(0);
		Shift[] ret = new Shift[N];
		for( i=0;i<N-M;i++ )
			ret[i] = info[i];
		ret[N-M]=result[M-1];
		for( i=N-M+1;i<N;i++ )
			ret[i]=result[i-N+M-1];
		return ret;
	}
	boolean verify(int print)
	{
		int i,j;
		Shift[] a = new Shift[N];
		Shift[] b = new Shift[M];
		for( i=0;i<M;i++ )
			b[i] = new Shift();
		for( i=0;i<N-M;i++ )
			a[i] = info[i];
		a[N-M]=result[M-1];
		for( i=N-M+1;i<N;i++ )
			a[i]=result[i-N+M-1];
		tCHECK.doit(a, b);
		if( (print&4) == 4)
		{
			Shift.printArray(a, "info+result:", 3);
		}
		for( i=0;i<M;i++ )
			for( j=0;j<Shift.size;j++ )
				if( b[i].buf[j]!=0 )
				{
					if( (print&2) == 2 )
					{
						System.out.println("Error:");
						Shift.printArray(b, "Verify Error:",3);
					}
					return false;	
				}
		if( (print&1) == 1)
		{
			Shift.printArray(a, "info+result:", 3);
		}
		return true;
	}
	public void setInfo(byte[] msg)
	{
		Shift.msg2shift(info, msg, (N-M)*bl);
	}
	public static void testEnc( QCRS ttu, int k)
	{
//		ttu.reportTask();
		Shift.clean(ttu.info);
		ttu.info[k/ttu.bl].setXk(k%ttu.bl);
		ttu.encShift(0xff);
		Shift.printArray(ttu.result,"Result: ", 3);
		ttu.verify(7);
	}
	
	public static void check125() {
		LDPCDB db = new LDPCDB();
		LDPCDBItem x = db.db.get(1);
		int bl = 256;
		int N = 104;
		int poly = 0x83;
		int SIM = 1;
		int i,j;
		if( x.getCode(N, bl, poly, null) != null)
		{
			System.out.printf("int[%d][%d] rs = {\n",x.code.length,x.code[0].length);
			for( i=0;i<x.code.length;i++ )
			{
				System.out.printf("{ ");
				for( j=0;j<x.code[i].length;j++ )
					System.out.printf("%4d, ",x.code[i][j]);
				System.out.printf("},\n");
			}
			System.out.printf("};\n");
		}
	}
	public static void main(String[] args) {
	
		check125();
		
		double df[] = {0.521814,    0.271293,     0.0,    0.206893};
		int dg[] = {   2,            3,            4,          5};
		int i,j;
		Degree deg = new Degree( dg,df );
		QCRS ttu = new QCRS( deg, 26, 52, 104, 0x43 );
		poly.out = new PrintWriter(System.out,true);
		
		int pos=0;
		ttu.genH(pos,0);
		while( ttu.calcPhyInv(0)==false )
		{
			ttu.swap(ttu.H,pos,ttu.M);
			pos++;
			if( pos> 10 )
				break;
		};
		if( pos<=10 )
		{	
			System.out.printf("Try %d\n",pos);
			System.out.printf("Inv :");
			ttu.phyInv.print();
			System.out.printf("\n");
		}
			
		System.out.printf("%d %d %d\n",ttu.N,ttu.M,ttu.bl);
		System.out.printf("int[][] rs = {\n");
		for( i=0;i<ttu.M;i++ )
		{
			System.out.printf("{ ");
			for( j=0;j<ttu.N;j++ )
				System.out.printf("%4d, ",ttu.H[i][j]);
			System.out.printf("},\n");
		}
		System.out.printf("};\n");
		
		int[][] rs = {
				{  228,  234,  256,  227,  253,  256,  256,  248,  256,  256,  256,    0,  256,  256,  256,  256,  256,  256,  256,  256, },
				{  256,   17,   13,  256,  247,  256,   15,  256,  256,  249,  256,  256,    0,  256,  256,  256,  256,  256,  256,  256, },
				{  256,  227,  252,  241,  256,  256,  256,  230,    2,  256,  256,  256,  256,    0,  256,  256,  256,  256,  256,  256, },
				{  256,   24,   29,  256,  256,    7,  256,   29,  256,  256,   30,  256,   20,  256,    0,  256,  256,  256,  256,  256, },
				{  256,  256,  245,  231,  256,  256,  256,  256,  232,  256,    0,  251,  256,  256,  256,    0,  256,  256,  256,  256, },
				{  256,  252,  256,    8,  256,   15,  251,  256,  256,  256,    7,  256,  256,   12,  256,  256,    0,  256,  256,  256, },
				{  256,  254,  256,   11,  256,    8,  256,  256,  256,   11,  240,  256,  256,  256,  256,  256,  256,    0,  256,  256, },
				{  236,  256,  234,    1,  256,  256,  245,  256,  256,  256,  252,  256,  256,  256,  256,  256,  256,  256,    0,  256, },
				{  256,  256,   15,  244,  256,  256,  256,  256,  256,  256,    4,  256,  256,  256,  256,  250,    7,   19,  256,    0, },
				{  256,  250,  235,  256,    3,  256,  256,  256,  252,  256,    1,  256,  256,  256,    0,  256,  256,  256,  240,  239, },
				};
		
		QCRS ttu2 = new QCRS( rs );
		
		for( i=0;i<ttu2.M*ttu.bl;i+=73 )
			QCRS.testEnc(ttu2, i);
	}	
}
