import pandas as pd
import matplotlib.pyplot as plt
import math
import matplotlib
matplotlib.rcParams.update({'font.size': 16})
from scipy.stats import ttest_ind


# homo3vs1
homo3vs1 = pd.DataFrame()
for i in range(1, 41):
	run_file = str(i)+'.stat'
	run = pd.read_csv("/Users/erotundo/git/UniNotes/ASO/statistics/homo.3v1/"+run_file, sep=' ', header=None, index_col=0)
	run = run.iloc[:,0:6].dropna()
	run.index.name = 'gen'
	run.columns = ['mean_dogs', 'bestOfGeneration_dogs', 'bestSoFar_dogs', 'mean_sheep', 'bestOfGeneration_sheep', 'bestSoFar_sheep']
	run = run['bestSoFar_dogs']
	homo3vs1[str(i)] = run
homo3vs1['std_error'] = homo3vs1.std(axis=1) / math.sqrt(40)
homo3vs1['ci_high'] =  (1.96 * homo3vs1['std_error'])
homo3vs1['ci_low'] =  (1.96 * homo3vs1['std_error'])



# hetero3vs1
hetero3vs1 = pd.DataFrame()
for i in range(1, 41):
	run_file = str(i)+'.stat'
	run = pd.read_csv("/Users/erotundo/git/UniNotes/ASO/statistics/hetero.2v1/"+run_file, sep=' ', header=None, index_col=0)
	run = run.iloc[:,0:9].dropna()
	run.index.name = 'gen'
	run.columns = ['mean_dog1', 'bestOfGeneration_dog1', 'bestSoFar_dog1', 'mean_dog2', 'bestOfGeneration_dog2', 'bestSoFar_dog2', 'mean_dog3', 'bestOfGeneration_dog3', 'bestSoFar_dog3']
	hetero3vs1[str(i)+"_1"] = run['bestSoFar_dog1']
	hetero3vs1[str(i)+"_2"] = run['bestSoFar_dog2']
	hetero3vs1[str(i)+"_3"] = run['bestSoFar_dog3']
hetero3vs1['std_error'] = hetero3vs1.std(axis=1) / math.sqrt(120)
hetero3vs1['ci_high'] =  (1.96 * hetero3vs1['std_error'])
hetero3vs1['ci_low'] =  (1.96 * hetero3vs1['std_error'])

fig, ax = plt.subplots(nrows=1, ncols=1)
ax.errorbar(homo3vs1.index, homo3vs1.mean(axis=1).values, yerr=pd.concat([homo3vs1['ci_low'], 
	homo3vs1['ci_high']], axis=1).T.values, fmt='o-', label='Homo')
ax.errorbar(hetero3vs1.index, hetero3vs1.mean(axis=1).values, yerr=pd.concat([hetero3vs1['ci_low'],hetero3vs1['ci_high']], axis=1).T.values, 
	fmt='o-', label='Hetero')
plt.ylim((0, 70000))
fig.suptitle('Homo vs Hetero - 3vs1 - 40 runs')
legend = ax.legend(loc='lower right', shadow=True)
ax.set_xlabel('Generation')
ax.set_ylabel('Fitness')
fig.tight_layout(pad=2)

## PRINT
plt.savefig("homo3v1-hetero3v1-bestSoFar.pdf")

## T-TEST
print ttest_ind(homo3vs1.mean(axis=1).values, hetero3vs1.mean(axis=1).values)
