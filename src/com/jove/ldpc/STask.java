package com.jove.ldpc;

import java.io.PrintWriter;

public class STask {

	/**
	 * @param args
	 */
	private class stMac 
	{
		int in;
		int mac;
		int ro;
		
		stMac()
		{
			
		}
		void dump( PrintWriter out )
		{
			out.printf("%d %d %d\n",in,mac,ro);
		}
	};

	public void dumpTask(PrintWriter out)
	{
		out.printf("%d\n",taskLen);
		for( int i=0;i<taskLen;i++ )
			task[i].dump( out );
	}
	public enum build { matrix, matrixT, guass, density };

	stMac[] task;
	int taskLen;
	STask()
	{
		taskLen = 0;
		task = null;
	}
	STask( int[][] rs, int row, int col, int bl, build type )
	{
		switch( type )
		{
			case matrix:
				matrixBuild( rs, row, col, bl, type );
				break;
			case matrixT:
				matrixBuild( rs, row, col, bl, type );
				break;
			case guass:
				guassBuild( rs, row, col, bl );
				break;
			default:
				taskLen = 0;
				break;
		}
	}

	STask( Shift[][] rs, int row, int col )
	{
		densityBuild( rs, row, col );
	}
	
	int matrixBuild( int c[][], int row, int col, int bl, build type )
	{
		int i,j;
		int pos=0;
		taskLen = sumWeight( c, row, col, bl, type );
		task = new stMac[taskLen];
		for( i=0;i<taskLen;i++ )
			task[i] = new stMac();
		for( i=0;i<row;i++ )
			for( j=0;j<col;j++ )
				if( c[i][j]!=bl )
				{
					task[pos].ro=c[i][j];
					if( type==build.matrix )
					{
						task[pos].in = j;
						task[pos].mac = i;
					}
					else
					{
						task[pos].in = i;
						task[pos].mac = j;
					}
					pos++;
				}
		return pos;
	}

	int guassBuild( int c[][], int row, int col, int bl )
	{
		int i,j;
		int pos=0;
		taskLen = sumWeight( c, row, col, bl, build.guass );
		task = new stMac[taskLen];
		for( i=0;i<taskLen;i++ )
			task[i] = new stMac();
		for( i=0;i<col;i++ )
			for( j=i+1;j<row;j++ )
			{
				if( c[i][j]!=bl )
				{
					task[pos].ro=c[i][j];
					task[pos].in = i;
					task[pos].mac = j;
					pos++;
				}
			}
		return pos;
	}

	int densityBuild( Shift c[][], int row, int col )
	{
		int i,j,l;
		int pos=0;
		taskLen = sumWeight( c, row, col );
		task = new stMac[taskLen];
		for( i=0;i<taskLen;i++ )
			task[i] = new stMac();
		for( i=0;i<row;i++ )
			for( j=0;j<col;j++ )
			{
				Shift s = c[i][j];
				for( l=0;l<Shift.length;l++ )
				{
					if( ((s.buf[l/32]>>(l%32))&1)==1 )
					{
						assert(pos<taskLen);
						task[pos].in=i;
						task[pos].mac=j;
						task[pos].ro=l;
						pos++;
					}
				}
			}
		return pos;
	}
	
	int sumWeight( Shift[][] c, int row, int col)
	{
		int i,j;
		int count = 0;
		for( i=0;i<row;i++ )
			for( j=0;j<col;j++ )
			{
				count += c[i][j].weight();
			}
		return count;
	}
	
	int sumWeight( int c[][], int row, int col, int bl, build type )
	{
		int i,j;
		int count=0;
		switch( type )
		{
			case matrix:
			case matrixT:
				for( i=0;i<row;i++ )
					for( j=0;j<col;j++ )
						if( c[i][j]!=bl )
							count++;
				break;
			case guass:
				for( i=0;i<col;i++ )
					for( j=i+1;j<row;j++ )
						if( c[i][j]!=bl )
							count++;
				break;
			default:
				break;
		}
		return count;
	}

	void  doit( Shift in[], Shift out[] )
	{
		int i;
		for( i=0;i<taskLen;i++ )
		{
			out[task[i].mac].mac(in[task[i].in], task[i].ro);
		}
	}
	void  doit( Shift in[], Shift out[], int offsetIn, int offsetOut )
	{
		int i;
		for( i=0;i<taskLen;i++ )
		{
			out[task[i].mac+offsetOut].mac(in[task[i].in+offsetIn], task[i].ro);
		}
	}
	void printfTask()
	{
		System.out.printf("task length =%d\n",taskLen);
		for( int i=0;i<taskLen;i++ )
		{
			System.out.printf("task[%d] is shift[%d] %d mac[%d]\n",i,task[i].in,task[i].ro,task[i].mac);
		}
	}
	void shiftOneTest( int s)
	{
		System.out.printf("Test SHift Once\n");
		Shift[] testUin = Shift.alloc(1);
		Shift[] testUout = Shift.alloc(1);
		
		int[][] c={ {s} };
		matrixBuild(c,1,1,Shift.length,build.matrix);
		printfTask();
		for( int testP = 0; testP<Shift.length; testP+=13 )
		{
			testUin[0].setXk(testP);
			Shift.zero(testUout);
			System.out.printf("\n");
			Shift.printArray(testUin,"In :",2);
			doit(testUin,testUout);
			System.out.printf("test at %d shift %d to %d \n",testUin[0].isXk(),s,testUout[0].isXk());
			Shift.printArray(testUout,"Out :",2);
			if( (testUin[0].isXk()-testUout[0].isXk()+Shift.length)%Shift.length!=s )
				System.out.printf("shift error at %d \n",testP );
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Shift.length = 136;
		Shift.init();
		STask ttu = new STask();
		ttu.shiftOneTest(3);
	}

}
