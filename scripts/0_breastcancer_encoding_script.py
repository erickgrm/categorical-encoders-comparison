# Main routine to encode a dataset with several encoders
# @author: github.com/erickgrm

#########################################################
# Change the following according to the name of the file to encode
# and separation character (',' or '\t' or other), 
filepath = '../datasets/0_breastcancer/' # Path to file
filename =  'breast_cancer.csv' 
separation = '\t'

target_flag = 1 # 1 if target variable present, 0 otherwise

# Get filename prefix to name encoded versions
name_prefix= filename.split('.')[0]
##########################################################


# Read file and print summary
import pandas as pd
from utils import *
dataset = pd.read_csv(filepath+filename, sep=separation, header=None) 

print('>> Encoding the', filename, 'dataset')
print('>> No of rows: ', len(dataset.iloc[0:,]))
print('>> No of variables:', len(dataset.columns))
print('>> No of categorical variables:', num_categorical_cols(dataset))
print('>> No of categorical instances:', num_categorical_instances(dataset), '\n')

# Prepare dataset
if target_flag == 1:
    # Separate target variable
    features = dataset.drop(dataset.columns[-1], axis=1)
    target = dataset.iloc[:,-1]
else: 
    features = dataset
    target = _


"""START: Import encoders"""
import category_encoders as ce
import sys
sys.path.append('../encoders/')
from ceng import CENGEncoder
from pattern_preserving import SimplePPEncoder, AgingPPEncoder, GeneticPPEncoder
from entity_embedding import EntityEmbeddingEncoder
from cesamo import CESAMOEncoder

Encoders = {'Ordinal': ce.OrdinalEncoder(),
            'Polynomial': ce.PolynomialEncoder(),
            'OneHot': ce.OneHotEncoder(),
            'BackwardDifference': ce.BackwardDifferenceEncoder(),
            'Helmert': ce.HelmertEncoder(),
            'EntityEmbedding': EntityEmbeddingEncoder(),
            'TargetEnc': ce.TargetEncoder(),
            'WOE': ce.WOEEncoder(),
            'CENG': CENGEncoder(),
            'GeneticPP': GeneticPPEncoder(estimator_name='LinearRegression', num_predictors=2),
            'AgingPP': AgingPPEncoder(estimator_name='LinearRegression', num_predictors=2),
            'SimplePP': SimplePPEncoder(estimator_name='LinearRegression', num_predictors=2),
            'CESAMOEncoder': CESAMOEncoder()}

if target_flag == 0:
    del Encoders['EntityEmbedding']
    del Encoders['TargetEnc']
    del Encoders['WOE']
"""END: Import encoders"""

import time
""" START: Apply encoders and save encoded dataset """
for key in Encoders:
    print('Encoding with the '+key+ ' Encoder')
    try: 
        tic = time.perf_counter() 

        df = Encoders[key].fit_transform(features, target)
        if target_flag == 1:
            df[len(df.columns)] = target
        # Save encoded file
        df.to_csv(filepath+'/encoded_examples/'+name_prefix+'_'+key+'.csv', sep=',', header=False, index=False)

        toc = time.perf_counter()
        print('>> Done in ',round(toc-tic, 3), ' seconds.')
        print('Saved to '+filepath+'encoded_examples/'+name_prefix+'_'+key+'.csv\n')
    except: 
        raise Exception('   Encoding with the '+key+' Encoder failed.\n')


""" END: Apply encoders and save encoded dataset"""
