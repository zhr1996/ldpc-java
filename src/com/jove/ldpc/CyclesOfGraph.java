package com.jove.ldpc;


public class CyclesOfGraph {

	int M;
	int N;
	int[] cyclesTable;
	int[][] H;
	private int[] tmp;
	private int[] med;
	private int[] tmpCycles;

	NodesOfGraph[] nodesOfGraph;
	CyclesOfGraph(int mm, int n, int[][] h)
	{
		int i, j, k, m, index;
		M=mm;
		N=n;
		H=h;

		tmp=new int [N];
		med=new int [N];
		tmpCycles=new int [N];
		cyclesTable=new int [N];
		nodesOfGraph=new NodesOfGraph [N];
		for(i=0;i<N;i++)
		{
		    index=0;
		    for(j=0;j<M;j++)
		    {
		    	if(H[j][i]==1)
		    	{
		    		tmp[index]=j;
		    		index++;
		    	}
		    }
		    nodesOfGraph[i].setSymbolConnections(index, tmp);
		}
		for(i=0;i<M;i++)
		{
		    index=0;
		    for(j=0;j<N;j++)
		    {
		    	if(H[i][j]==1)
		    	{
		    		tmp[index]=j;
		    		index++;
		    	}
		    }
		    nodesOfGraph[i].setParityConnections(index, tmp);
		}
		for(i=0;i<N;i++)
		{
		    index=0;
		    for(j=0;j<nodesOfGraph[i].numOfSymbolConnections;j++)
		    {
		    	for(k=0;k<nodesOfGraph[nodesOfGraph[i].symbolConnections[j]].numOfParityConnections;k++)
		    	{
		    		int t=0;
		    		for(m=0;m<index;m++)
		    		{
		    			if(nodesOfGraph[nodesOfGraph[i].symbolConnections[j]].parityConnections[k]==tmp[m])
		    			{
		    				t=1; 
		    				break;
		    			}
		    		}
		    		if(nodesOfGraph[nodesOfGraph[i].symbolConnections[j]].parityConnections[k]==i) 
		    			t=1;
		    		if(t==0) 
		    		{
		    			tmp[index]=nodesOfGraph[nodesOfGraph[i].symbolConnections[j]].parityConnections[k];
		    			index++;
		    		}
		    	}
		    }
		    nodesOfGraph[i].setSymbolMapping(index, tmp);
		}
	}
	void getCyclesTable()
	{
		int i, j, k, m, n, t, imed;
		for(i=0;i<N;i++)
		{
			if(nodesOfGraph[i].numOfSymbolConnections<=1) 
			{
				cyclesTable[i]=2*N; 
				continue;
			}
			for(j=0;j<nodesOfGraph[i].numOfSymbolConnections-1;j++){ //-1 because the graph is undirected
				for(k=0;k<nodesOfGraph[nodesOfGraph[i].symbolConnections[j]].numOfParityConnections;k++)
				{
					tmp[k]=nodesOfGraph[nodesOfGraph[i].symbolConnections[j]].parityConnections[k];
				}
				int cycles=2;
				int index=nodesOfGraph[nodesOfGraph[i].symbolConnections[j]].numOfParityConnections;
				LOOP:
				while(true)
				{
					imed=0;
					for(k=0;k<index;k++)
					{
						if(tmp[k]==i) continue;
						for(m=0;m<nodesOfGraph[tmp[k]].numOfSymbolConnections;m++)
						{
							for(n=0;n<nodesOfGraph[i].numOfSymbolConnections;n++)
							{
								if((n!=j)&&(nodesOfGraph[tmp[k]].symbolConnections[m]==nodesOfGraph[i].symbolConnections[n]))
								{
									cycles+=2;
									break LOOP;
								}
							}
						}
						for(m=0;m<nodesOfGraph[tmp[k]].numOfSymbolMapping;m++)
						{
							t=0;
							for(int l=0;l<imed;l++) 
							{
								if(nodesOfGraph[tmp[k]].symbolMapping[m]==med[l])
								{
									t=1; break;
								}
							}
							if(t==0)
							{
								med[imed]=nodesOfGraph[tmp[k]].symbolMapping[m];
								imed++;
							}
						}
					}
					index=imed;
					for(k=0;k<index;k++) 
					{
						tmp[k]=med[k];//cout<<tmp[k]<<" ";
					}
					cycles+=2;
					if(cycles>=2*N)  
						break LOOP;
				}
				tmpCycles[j]=cycles;
			}
			cyclesTable[i]=tmpCycles[0];
			for(j=1;j<nodesOfGraph[i].numOfSymbolConnections-1;j++)
			{
				if(cyclesTable[i]>tmpCycles[j])
					cyclesTable[i]=tmpCycles[j];
			}
		}	
	}
	void printCyclesTable()
	{
		int i;
		int[] temp = new int[20];
		for(i=0;i<20;i++) temp[i]=0;
		for(i=0;i<N;i++)
		{
			if(cyclesTable[i]==4) temp[0]++;
			else if(cyclesTable[i]==6) temp[1]++;    
			else if(cyclesTable[i]==8) temp[2]++;
			else if(cyclesTable[i]==10) temp[3]++;
			else if(cyclesTable[i]==12) temp[4]++;
			else if(cyclesTable[i]==14) temp[5]++;
			else if(cyclesTable[i]==16) temp[6]++;
			else if(cyclesTable[i]==18) temp[7]++;
			else if(cyclesTable[i]==20) temp[8]++;
			else if(cyclesTable[i]==22) temp[9]++;
			else if(cyclesTable[i]==24) temp[10]++;
			else if(cyclesTable[i]==26) temp[11]++;
			else if(cyclesTable[i]==28) temp[12]++;
			else if(cyclesTable[i]==30) temp[13]++;
			else {
				System.out.println("Wrong cycles calculation   "+cyclesTable[i]);
			}
		}  
		System.out.println();
		System.out.println("Num of Nodes with local girth   4:  "+temp[0]);
		System.out.println("Num of Nodes with local girth   6:  "+temp[1]);
		System.out.println("Num of Nodes with local girth   8:  "+temp[2]);
		System.out.println("Num of Nodes with local girth   10:  "+temp[3]);
		System.out.println("Num of Nodes with local girth   12:  "+temp[4]);
		System.out.println("Num of Nodes with local girth   14:  "+temp[5]);
		System.out.println("Num of Nodes with local girth   16:  "+temp[6]);
		System.out.println("Num of Nodes with local girth   18:  "+temp[7]);
		System.out.println("Num of Nodes with local girth   20:  "+temp[8]);
		System.out.println("Num of Nodes with local girth   22:  "+temp[9]);
		System.out.println("Num of Nodes with local girth   24:  "+temp[10]);
		System.out.println("Num of Nodes with local girth   26:  "+temp[11]);
		System.out.println("Num of Nodes with local girth   28:  "+temp[12]);
		System.out.println("Num of Nodes with local girth   30:  "+temp[13]);
	}
	int girth()
	{  
		int girth=2*N;
		for(int i=0;i<N;i++) 
			if(girth>cyclesTable[i]) 
				girth=cyclesTable[i];
		return(girth);
	}
	  
	  public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

