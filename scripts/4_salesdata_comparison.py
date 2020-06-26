# Main routine for comparison of categorical encoders for unsupervised learning
# @author: github.com/erickgrm

#########################################################
# Change the following according to the name of the file to encode
# and separation character (',' or '\t')
filepath = '../datasets/4_salesdata/'   # Path to file
filename = 'salesdata_cluster.csv'      # .txt, .csv, .data or other
separation = ','                        # , or \t
n_clusters=  5                         # integer
##########################################################
# Get filename prefix
name_prefix= filename.split('.')[0]

# Read file and print summary
import pandas as pd
from utils import *
dataset = pd.read_csv(filepath+filename, sep=separation, header=None) 
print('>> Evaluating encoders on the', filename, 'dataset')
print('>> No of rows: ', len(dataset.iloc[0:,0]))
print('>> No of variables:', len(dataset.iloc[0,0:])-1)
print('>> No of categorical variables:', num_categorical_cols(dataset))
print('>> No of categorical instances:', num_categorical_instances(dataset))
print('Results are saved to ../results/'+name_prefix+'_results.txt')

# Separate target variable
features = dataset.drop(dataset.columns[-1], axis=1)
target = dataset.iloc[:,-1]

import warnings
warnings.filterwarnings("ignore")

"""START: Import encoders"""
import category_encoders as ce
import sys
sys.path.append('../encoders/')
from ceng import CENGEncoder
from cesamo import CESAMOEncoder
from entity_embedding import EntityEmbeddingEncoder
from pattern_preserving import SimplePPEncoder, AgingPPEncoder, GeneticPPEncoder

Encoders = {'Ordinal': ce.OrdinalEncoder(),
            'Polynomial': ce.PolynomialEncoder(),
            'OneHot': ce.OneHotEncoder(),
            'BackwardDifference': ce.BackwardDifferenceEncoder(),
            'Helmert': ce.HelmertEncoder(),
            'EntityEmbedding': EntityEmbeddingEncoder(),
            'TargetEnc': ce.TargetEncoder(),
            'CENG': CENGEncoder(verbose = 0),
            'GeneticPP': GeneticPPEncoder(),
            'AgingPP': AgingPPEncoder(),
            'SimplePP': SimplePPEncoder(),
            'CESAMOEncoder': CESAMOEncoder()}
"""END: Import encoders"""

"""START: Import models"""
try: 
    from sklearn.cluster import KMeans, SpectralClustering, AgglomerativeClustering #Birch DBSCAN
except:
    raise Exception('Scikit-Learn 0.22.2+ not available')

Models = {'K-Means': KMeans(n_clusters),
          'Spectral': SpectralClustering(n_clusters),
          'Agglomerative': AgglomerativeClustering(n_clusters=n_clusters)}
          #'DBSCAN': DBSCAN(eps=0.3, min_samples=15)}

"""END: Import models"""


# Performance evaluation function 
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import adjusted_mutual_info_score as ami
from sklearn.metrics import calinski_harabasz_score as chs
from sklearn.metrics import silhouette_score as sil
import time
def performance(encoder, models, K):
    mean_ami = dict(zip(models.keys(), list(np.zeros(len(models)))))
    mean_chs = dict(zip(models.keys(), list(np.zeros(len(models)))))
    mean_sil = dict(zip(models.keys(), list(np.zeros(len(models)))))

    tic = time.perf_counter()
    for i in range(K):
        features_enc = encoder.fit_transform(features, target)

        for key in models:
            model = models[key]
            
            y_predict = model.fit_predict(features_enc, target)
            c = 0
            while len(np.unique(y_predict)) <= 1 and c < 10:
                y_predict = model.fit_predict(features_enc, target)
                c +=1

            mean_ami[key] += ami(target, y_predict)/K
            mean_chs[key] += chs(features_enc, y_predict)/K
            mean_sil[key] += sil(features_enc, y_predict, metric='euclidean')/K

    toc = time.perf_counter()

    # Write results to file
    res = open('../results/'+name_prefix+'_results.txt', 'a')
    res.write(type(encoder).__name__[0:-7]+' Encoder\n')
    for key in mean_ami:
        res.write(' '+key+': '+str(mean_ami[key])+', '+str(mean_chs[key])+', '+str(mean_sil[key])+'\n')
    res.write('Total time: '+str(round(toc-tic,3))+'\n') 
    res.close()

    print('Evaluation of', type(encoder).__name__[0:-7], 'Encoder completed in', round(toc-tic,3),'s')


"""START: Evaluation of encoders"""
K = 3 #Meta-parameter

from multiprocessing import Pool, Process, cpu_count
print('>> No of available cpu-cores:',  cpu_count(),'\n')
pool = Pool(cpu_count())

# Create results file
results = open('../results/'+name_prefix+'_results.txt', 'a')
results.close()

# Main cycle
for encoder in Encoders:
    Process(target=performance, args=(Encoders[encoder], Models, K)).start()

"""END: Evaluation of encoders"""
