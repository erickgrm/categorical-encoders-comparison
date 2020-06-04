# Main routine to encode a dataset with several encoders
# @author: github.com/erickgrm

#################### USER INPUT #########################
# Change the following according to the file to encode
filepath = '../datasets/ /'         # Path to file
filename =  ' '                     # .txt, .csv, .data or other 
separation = ' '                    # , or \t
target_flag = True                  # Set to True if target variable present

##########################################################
# Get filename prefix to name encoded versions
name_prefix= filename.split('.')[0]

# Read file and print summary
import pandas as pd
from utils import *
dataset = pd.read_csv(filepath+filename, sep=separation, header=None) 

print('>> Encoding the', filename, 'dataset')
print('>> No of rows: ', len(dataset.iloc[0:,]))
print('>> No of variables:', len(dataset.columns))
print('>> No of categorical variables:', num_categorical_cols(dataset))
print('>> No of categorical instances:', num_categorical_instances(dataset))
print('All encoded versions are saved to '+filepath+'encoded_examples/\n')

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
            'CENG': CENGEncoder(verbose=0),
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
def apply_encoder(X, y, encoder, target_flag):

    tic = time.perf_counter() 
    X_enc = encoder.fit_transform(X, y)
    if target_flag:
        X_enc[len(X_enc.columns)] = y
    toc = time.perf_counter()

    # Save encoded file
    X_enc.to_csv(filepath+'/encoded_examples/'+name_prefix+'_'+key+'.csv', sep=',', header=False, index=False)
    print('>> Applied',key,'Encoder in',round(toc-tic, 3), 'seconds.')

    return round(toc-tic,3)


""" START: Apply encoders and save encoded dataset """
from multiprocessing import Pool, Process, cpu_count
print('>> No of available cpu-cores:', cpu_count(), '\n')
pool = Pool(cpu_count())

for key in Encoders:
    try:
        Process(target=apply_encoder, args=(features, target, Encoders[key], target_flag)).start()
    except: 
        raise Exception('>> Encoding with the '+key+' Encoder failed.\n')

""" END: Apply encoders and save encoded dataset"""
