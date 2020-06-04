# Main routine for comparison of categorical encoders for supervised learning
# @author: github.com/erickgrm

#########################################################
# Change the following according to the name of the file to encode
# and separation character (',' or '\t')
filepath = '../datasets/5_tester/' # Relative path to file
filename = 'tester.csv' 
separation = '\t'

# Get filename prefix
name_prefix= filename.split('.')[0]
##########################################################

# Read file and print summary
import pandas as pd
from utils import *
dataset = pd.read_csv(filepath+filename, sep=separation, header=None) 

print('>> Evaluating encoders on the', filename, 'dataset')
print('>> No of rows: ', len(dataset.iloc[0:,0]))
print('>> No of variables:', len(dataset.iloc[0,0:])-1)
print('>> No of categorical variables:', num_categorical_cols(dataset))
print('>> No of categorical instances:', num_categorical_instances(dataset))
print('All encoded versions are saved to '+filepath+'encoded_examples/\n')

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
            'WOE': ce.WOEEncoder(),
            'CENG': CENGEncoder(verbose = 0),
            'GeneticPP': GeneticPPEncoder(estimator_name='LinearRegression', num_predictors=2),
            'AgingPP': AgingPPEncoder(estimator_name='LinearRegression', num_predictors=2),
            'SimplePP': SimplePPEncoder(estimator_name='LinearRegression', num_predictors=2),
            'CESAMOEncoder': CESAMOEncoder()}
"""END: Import encoders"""


'''START: Import models'''
try: 
    import sklearn.linear_model as lm
    import sklearn.svm as svm
    from sklearn.neighbors import KNeighborsClassifier
    from sklearn.naive_bayes import GaussianNB
    from sklearn.ensemble import RandomForestClassifier
    from sklearn.neural_network import MLPClassifier
    from sklearn.gaussian_process.kernels import RBF
    rbf_kernel = 1.0 * RBF(1.0)
except:
    raise Exception('Scikit-Learn 0.22.2+ not available')

Models = {'Naïve Bayes': GaussianNB(),
        'Linear Regression': lm.LinearRegression(), 
        #'Polynomial Regression': PolynomialRegression(max_degree=5),
        'Logistic Regression': lm.LogisticRegression(),
        'Linear SVM': svm.LinearSVC(),
        'Radial SVM': svm.SVC(kernel='rbf'),
        'K-Neighbours (K=7)': KNeighborsClassifier(),
        'Random Forest (n=50)': RandomForestClassifier(n_estimators=50),
        'Neural Network': MLPClassifier(max_iter=1000, hidden_layer_sizes=(50, 20), early_stopping=True)}
'''END: Import models'''


#Main performance evaluation function 
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import roc_auc_score as auc
import time
def performance(encoder, models, K):
    mean_auc = dict(zip(models.keys(), list(np.zeros(len(models)))))

    tic = time.perf_counter()
    for i in range(K):
        good_split = False
        while not(good_split):
            X_train, X_test, y_train, y_test = train_test_split(features, target, test_size=0.30)
            # Ensure the split is good in case of small or unbalanced datasets
            if 1 < len(np.unique(y_train)) and 1 < len(np.unique(y_test)):
                good_split = True

        X_train_enc = encoder.fit_transform(X_train, y_train)

        for key in models:
            model = models[key]
            model.fit(X_train_enc, y_train) 

            X_test_enc = encoder.transform(X_test)
            y_test_predict = model.predict(X_test_enc)
            mean_auc[key] += auc(y_test, y_test_predict)/K

    toc = time.perf_counter()

    # Write results to file
    res = open('../results/'+name_prefix+'_results.txt', 'a')
    res.write(type(encoder).__name__[0:-7]+' Encoder\n')
    for key in mean_auc:
        res.write(' '+key+': '+str(mean_auc[key])+'\n')
    res.close()

    print('Evaluation of', type(encoder).__name__[0:-7], 'Encoder completed in', round(toc-tic,3))


"""START: Evaluation of encoders"""
K = 3 #Meta-parameter

from multiprocessing import Pool, Process, cpu_count
print('>> No of available cpu-cores:',  cpu_count(),'\n')
pool = Pool(cpu_count())

# Create results file
results = open('../results/'+name_prefix+'_results.txt', 'w')
results.close()

# Main cycle
for encoder in Encoders:
    Process(target=performance, args=(Encoders[encoder], Models, K)).start()

"""END: Evaluation of encoders"""


