import java.io.Serializable;
import java.util.ArrayList;

import javafx.util.Pair;

import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split
	
	//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;
	
	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf



		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}



		// This method takes in a data list (ArrayList of type datum) and a minSizeInClassification (int) and returns
		// the calling DTNode object as the root of a decision tree trained using the data points present in the data list variable
		// Also, KEEP IN MIND that the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
		DTNode fillDTNode(ArrayList<Datum> datalist)
		{		
			int l = datalist.get(0).y; // initial label
			boolean condition = true; // whether or not all labels match
			
			for(Datum d : datalist) 
			{
				if(d.y != l) {
					condition = false;
					break;
				}
			}
			
			// we choose a good threshold since all labels are same just use leaf node
			if(condition == true) {
				DTNode n = new DTNode();
				n.label = l;
				return n;
			}
			
			// if not enough data to make efficient decision
			//   return based on majority label count
			//   avoids overfitting
			if(datalist.size() < minSizeDatalist)
			{
				DTNode n = new DTNode();
				n.label = findMajority(datalist);
				return n;
			}
			
			
			double bestAvgEntropy = Double.POSITIVE_INFINITY;
			int bestAttr = -1;
			double bestThreshold = -1;
			
			// temporary lists to hold data split
			ArrayList<Datum> d1 = new ArrayList<Datum>();
			ArrayList<Datum> d2 = new ArrayList<Datum>();
			
			
			// once best threshold has been chosen, these arrays will hold final data split
			ArrayList<Datum> f_d1 = new ArrayList<Datum>();
			ArrayList<Datum> f_d2 = new ArrayList<Datum>();
			
			
			for(int i = 0; i < 2; i++)
			{
				for(Datum d : datalist)
				{
					double t = d.x[i]; // The threshold that we will use for our test
					
					d1.clear();
					d2.clear();
					
					// split data using test threshold into temporary lists
					for(Datum g : datalist)
					{
						if(g.x[i] < t) d1.add(g);
						else d2.add(g);
					}
					
					// calculate entropy for test threshold
					double H = ((d1.size() * calcEntropy(d1)) / datalist.size()) + ((d2.size() * calcEntropy(d2))/ datalist.size());
					
					// if entropy is lower than what we have currently
					// update tracking variables
					if(H < bestAvgEntropy)
					{
						bestAvgEntropy = H;
						bestThreshold = t;
						bestAttr = i;
					}
				}
			}
			// Best threshold should have been selected by this point
			
			
			DTNode n = new DTNode();
			
			n.leaf = false;
			n.attribute = bestAttr;
			n.threshold = bestThreshold;
			
			
			// Here we are splitting data according to best threshold
			for(Datum d : datalist)
			{
				if(d.x[bestAttr] < bestThreshold) f_d1.add(d);
				else f_d2.add(d);
			}
			
			// fill up the child nodes using the logic above
			n.left = fillDTNode(f_d1);
			n.right = fillDTNode(f_d2);
			
			return n;
		}
		
		
		
		
		
		
		
		
		
		
		
		
		


		//This is a helper method. Given a datalist, this method returns the label that has the most
		//occurances. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist)
		{
			int l = datalist.get(0).x.length;
			int [] votes = new int[l];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}
			int max = -1;
			int max_index = -1;
			//find the label with the max occurrences
			for (int i = 0 ; i < l ;i++)
			{
				if (max<votes[i])
				{
					max = votes[i];
					max_index = i;
				}
			}
			return max_index;
		}



		// Datum = Data item
		
		// This method takes in a data point (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) 
		{
			//If Leaf, return label
			if (this.leaf)
			{
				return label;
			}
			
			//Comaprison of attribute and threshold + Recursion Call
			if (xQuery[this.attribute] < this.threshold)
			{
				return this.left.classifyAtNode(xQuery);
			}
			//Comaprison of attribute and threshold + Recursion Call
			if (xQuery[this.attribute] >= this.threshold)
			{
				return this.right.classifyAtNode(xQuery);
			}

			
			return label; 
		}

		

		//given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
		//at DTNode object passed as the parameter
		public boolean equals(Object dt2)
		{
			//Casting dt2 to a DTNode
			DTNode other_node = (DTNode) dt2;
			
			if(other_node == null) return false;
			
			// base leaf case
			if(this.leaf)
			{
				if(other_node.leaf)
				{
					return other_node.label == this.label;
				}
				return false;
			}
			
			
			// internal check
			if(other_node.attribute == this.attribute && other_node.threshold == this.threshold)
			{
				if(other_node.left == null || other_node.right == null || this.left == null || this.right == null) return false;
				
				// preorder child node check
				if(this.left.equals(other_node.left))
				{
					return this.right.equals(other_node.right);
				}
			}
			return false;
			
		}
	}



	//Given a dataset, this retuns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist)
	{
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
	int classify(double[] xQuery ) {
		DTNode node = this.rootDTNode;
		return node.classifyAtNode( xQuery );
	}

    // Checks the performance of a DecisionTree on a dataset
    //  This method is provided in case you would like to compare your
    //results with the reference values provided in the PDF in the Data
    //section of the PDF

    String checkPerformance( ArrayList<Datum> datalist)
	{
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

}
